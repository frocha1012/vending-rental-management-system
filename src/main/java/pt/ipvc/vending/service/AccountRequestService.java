package pt.ipvc.vending.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.ipvc.vending.domain.entity.AccountRequest;
import pt.ipvc.vending.domain.entity.Cliente;
import pt.ipvc.vending.domain.enums.AuditAction;
import pt.ipvc.vending.domain.enums.EstadoAccountRequest;
import pt.ipvc.vending.domain.enums.EstadoCliente;
import pt.ipvc.vending.repository.AccountRequestRepository;
import pt.ipvc.vending.repository.ClienteRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class AccountRequestService {

    private final AccountRequestRepository requestRepo;
    private final ClienteRepository clienteRepo;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    private final EmailService emailService;

    public AccountRequestService(AccountRequestRepository requestRepo,
                                 ClienteRepository clienteRepo,
                                 BCryptPasswordEncoder passwordEncoder,
                                 AuditLogService auditLogService,
                                 EmailService emailService) {
        this.requestRepo     = requestRepo;
        this.clienteRepo     = clienteRepo;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
        this.emailService    = emailService;
    }

    // ── Public submission ─────────────────────────────────────────────────────

    /**
     * Validates and persists a new account request from a prospective client.
     * Throws {@link IllegalArgumentException} with a user-friendly message on
     * any validation failure. Passwords are stored as a BCrypt hash.
     */
    public AccountRequest submeter(String nome, String nif, String email,
                                   String telefone, String morada,
                                   String username, String rawPassword,
                                   String confirmPassword) {

        if (nome == null || nome.isBlank())
            throw new IllegalArgumentException("O nome é obrigatório.");
        if (nif == null || nif.isBlank())
            throw new IllegalArgumentException("O NIF é obrigatório.");
        if (email == null || email.isBlank())
            throw new IllegalArgumentException("O email é obrigatório.");
        if (username == null || username.isBlank())
            throw new IllegalArgumentException("O username é obrigatório.");
        if (rawPassword == null || rawPassword.isBlank())
            throw new IllegalArgumentException("A password é obrigatória.");
        if (!rawPassword.equals(confirmPassword))
            throw new IllegalArgumentException("As passwords não coincidem.");

        // Username uniqueness — do not reveal which entity holds it
        if (clienteRepo.existsByUsername(username.trim()) ||
            requestRepo.existsByUsernameRequestedAndEstado(username.trim(), EstadoAccountRequest.PENDENTE)) {
            throw new IllegalArgumentException("Este username já está em uso. Escolha outro.");
        }

        // NIF uniqueness
        if (clienteRepo.existsByNif(nif.trim()) ||
            requestRepo.existsByNifAndEstado(nif.trim(), EstadoAccountRequest.PENDENTE)) {
            throw new IllegalArgumentException("Já existe uma conta associada a este NIF.");
        }

        // Email uniqueness
        if (clienteRepo.existsByEmail(email.trim()) ||
            requestRepo.existsByEmailAndEstado(email.trim(), EstadoAccountRequest.PENDENTE)) {
            throw new IllegalArgumentException("Já existe uma conta associada a este email.");
        }

        AccountRequest req = new AccountRequest();
        req.setNome(nome.trim());
        req.setNif(nif.trim());
        req.setEmail(email.trim());
        req.setTelefone(telefone != null && !telefone.isBlank() ? telefone.trim() : null);
        req.setMorada(morada != null && !morada.isBlank() ? morada.trim() : null);
        req.setUsernameRequested(username.trim());
        req.setPasswordHash(passwordEncoder.encode(rawPassword));
        req.setEstado(EstadoAccountRequest.PENDENTE);
        req.setDataPedido(LocalDate.now());

        AccountRequest saved = requestRepo.save(req);

        AuditContext.setActor("PUBLIC", nome.trim());
        auditLogService.logCustomAction(AuditAction.ACCOUNT_REQUEST_SUBMITTED,
                "AccountRequest", saved.getId(),
                "Pedido de conta submetido: username='" + username.trim()
                        + "', NIF=" + nif.trim());
        return saved;
    }

    // ── BackOffice queries ────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<AccountRequest> listarPendentes() {
        return requestRepo.findByEstadoOrderByDataPedidoDesc(EstadoAccountRequest.PENDENTE);
    }

    @Transactional(readOnly = true)
    public List<AccountRequest> listarTodos() {
        return requestRepo.findAllByOrderByDataPedidoDesc();
    }

    // ── Approve ───────────────────────────────────────────────────────────────

    /**
     * Approves a pending request: creates a new {@link Cliente} with the
     * requested credentials and marks the request as APROVADO.
     * After the DB transaction commits, an approval email is sent to the
     * client's email address. A mail failure never rolls back the approval.
     */
    public Cliente aprovar(Long id) {
        AccountRequest req = getOrThrow(id);

        if (req.getEstado() != EstadoAccountRequest.PENDENTE) {
            throw new IllegalStateException("Este pedido já foi processado.");
        }

        Cliente cliente = new Cliente();
        cliente.setNome(req.getNome());
        cliente.setNif(req.getNif());
        cliente.setEmail(req.getEmail());
        cliente.setTelefone(req.getTelefone());
        cliente.setMorada(req.getMorada());
        cliente.setUsername(req.getUsernameRequested());
        cliente.setPassword(req.getPasswordHash());
        cliente.setEstado(EstadoCliente.ATIVO);
        cliente.setDataRegisto(LocalDate.now());

        Cliente savedCliente = clienteRepo.save(cliente);

        req.setEstado(EstadoAccountRequest.APROVADO);
        requestRepo.save(req);

        auditLogService.logCustomAction(AuditAction.ACCOUNT_REQUEST_APPROVED,
                "AccountRequest", req.getId(),
                "Pedido aprovado: '" + req.getUsernameRequested()
                        + "' — cliente criado com ID " + savedCliente.getId());

        auditLogService.logCreate("Cliente", savedCliente.getId(),
                "Cliente criado via aprovação de pedido de conta: "
                        + savedCliente.getNome() + " (NIF: " + savedCliente.getNif() + ")");

        // Send email outside the transaction boundary — failure must never roll back the approval.
        sendApprovalEmailSafely(req);

        return savedCliente;
    }

    /**
     * Sends the approval email and writes an audit entry for the outcome.
     * Exceptions are swallowed so a mail error never propagates to the caller.
     */
    private void sendApprovalEmailSafely(AccountRequest req) {
        try {
            boolean sent = emailService.sendAccountApprovedEmail(
                    req.getEmail(), req.getNome(), req.getUsernameRequested());

            if (sent) {
                auditLogService.logCustomAction(AuditAction.ACCOUNT_REQUEST_APPROVED,
                        "AccountRequest", req.getId(),
                        "Email de aprovação enviado para: " + req.getEmail());
            } else {
                auditLogService.logCustomAction(AuditAction.ACCOUNT_REQUEST_APPROVED,
                        "AccountRequest", req.getId(),
                        "AVISO: conta aprovada mas o email de notificação falhou para: " + req.getEmail());
            }
        } catch (Exception ex) {
            auditLogService.logCustomAction(AuditAction.ACCOUNT_REQUEST_APPROVED,
                    "AccountRequest", req.getId(),
                    "AVISO: conta aprovada mas o email de notificação falhou para: "
                            + req.getEmail() + " — " + ex.getMessage());
        }
    }

    // ── Reject ────────────────────────────────────────────────────────────────

    public void rejeitar(Long id, String observacoes) {
        AccountRequest req = getOrThrow(id);

        if (req.getEstado() != EstadoAccountRequest.PENDENTE) {
            throw new IllegalStateException("Este pedido já foi processado.");
        }

        req.setEstado(EstadoAccountRequest.REJEITADO);
        if (observacoes != null && !observacoes.isBlank()) {
            req.setObservacoes(observacoes.trim());
        }
        requestRepo.save(req);

        auditLogService.logCustomAction(AuditAction.ACCOUNT_REQUEST_REJECTED,
                "AccountRequest", req.getId(),
                "Pedido rejeitado: '" + req.getUsernameRequested() + "'"
                        + (observacoes != null && !observacoes.isBlank()
                           ? " — motivo: " + observacoes.trim() : ""));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private AccountRequest getOrThrow(Long id) {
        return requestRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado: " + id));
    }
}
