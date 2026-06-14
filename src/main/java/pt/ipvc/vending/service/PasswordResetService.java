package pt.ipvc.vending.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.ipvc.vending.domain.entity.Cliente;
import pt.ipvc.vending.domain.entity.PasswordResetToken;
import pt.ipvc.vending.domain.enums.AuditAction;
import pt.ipvc.vending.domain.enums.EstadoCliente;
import pt.ipvc.vending.repository.ClienteRepository;
import pt.ipvc.vending.repository.PasswordResetTokenRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class PasswordResetService {

    private static final int TOKEN_EXPIRY_MINUTES = 30;

    private final ClienteRepository clienteRepo;
    private final PasswordResetTokenRepository tokenRepo;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    private final EmailService emailService;

    public PasswordResetService(ClienteRepository clienteRepo,
                                PasswordResetTokenRepository tokenRepo,
                                BCryptPasswordEncoder passwordEncoder,
                                AuditLogService auditLogService,
                                EmailService emailService) {
        this.clienteRepo     = clienteRepo;
        this.tokenRepo       = tokenRepo;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
        this.emailService    = emailService;
    }

    // ── Request reset ─────────────────────────────────────────────────────────

    /**
     * Processes a forgot-password request. Always returns silently — the caller
     * must show a generic message regardless of the outcome so that an attacker
     * cannot enumerate valid username/email combinations.
     */
    public void requestReset(String username, String email) {
        AuditContext.setActor("PUBLIC", username != null ? username : "?");

        Optional<Cliente> opt = clienteRepo.findByUsername(username != null ? username.trim() : "");

        // Generic audit — do not reveal whether account exists
        auditLogService.logCustomAction(AuditAction.PASSWORD_RESET_REQUESTED,
                "Cliente", null,
                "Pedido de reset de password para username: '"
                        + (username != null ? username.trim() : "") + "'");

        if (opt.isEmpty()) {
            return;
        }

        Cliente cliente = opt.get();

        // Silent no-op if email does not match or account is not active
        if (!cliente.getEmail().equalsIgnoreCase(email != null ? email.trim() : "")
                || cliente.getEstado() != EstadoCliente.ATIVO) {
            return;
        }

        // Invalidate any previous unused tokens for this client
        tokenRepo.invalidatePreviousTokens(cliente.getId());

        // Issue new token
        PasswordResetToken prt = new PasswordResetToken();
        prt.setToken(UUID.randomUUID().toString());
        prt.setCliente(cliente);
        prt.setExpiresAt(LocalDateTime.now().plusMinutes(TOKEN_EXPIRY_MINUTES));
        prt.setUsed(false);
        PasswordResetToken saved = tokenRepo.save(prt);

        // Send email — failure must not surface to the user
        emailService.sendPasswordResetEmail(cliente.getEmail(), cliente.getNome(), saved.getToken());
    }

    // ── Validate token ────────────────────────────────────────────────────────

    /**
     * Returns the token entity if it is valid (exists, not used, not expired).
     * Returns empty otherwise.
     */
    @Transactional(readOnly = true)
    public Optional<PasswordResetToken> validateToken(String token) {
        if (token == null || token.isBlank()) return Optional.empty();
        return tokenRepo.findByToken(token)
                .filter(t -> !t.isUsed())
                .filter(t -> t.getExpiresAt().isAfter(LocalDateTime.now()));
    }

    // ── Complete reset ────────────────────────────────────────────────────────

    /**
     * Validates the token and sets the new password.
     *
     * @throws IllegalArgumentException with a user-friendly message on any failure.
     */
    public void completeReset(String token, String newPassword, String confirmPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("A nova password não pode estar vazia.");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("As passwords não coincidem.");
        }

        PasswordResetToken prt = tokenRepo.findByToken(token != null ? token : "")
                .orElse(null);

        AuditContext.setActor("PUBLIC", "reset-flow");

        if (prt == null || prt.isUsed()) {
            auditLogService.logCustomAction(AuditAction.PASSWORD_RESET_FAILED,
                    "PasswordResetToken", null,
                    "Reset inválido: token inexistente ou já utilizado.");
            throw new IllegalArgumentException("Este link de reset é inválido ou já foi utilizado.");
        }

        if (prt.getExpiresAt().isBefore(LocalDateTime.now())) {
            auditLogService.logCustomAction(AuditAction.PASSWORD_RESET_FAILED,
                    "PasswordResetToken", prt.getId(),
                    "Reset inválido: token expirado para cliente ID " + prt.getCliente().getId());
            throw new IllegalArgumentException("Este link de reset expirou. Solicite um novo.");
        }

        Cliente cliente = prt.getCliente();
        cliente.setPassword(passwordEncoder.encode(newPassword));
        clienteRepo.save(cliente);

        prt.setUsed(true);
        tokenRepo.save(prt);

        AuditContext.setActor("CLIENTE", cliente.getNome());
        auditLogService.logCustomAction(AuditAction.PASSWORD_RESET_COMPLETED,
                "Cliente", cliente.getId(),
                "Password redefinida com sucesso via link de reset: " + cliente.getUsername());
    }
}
