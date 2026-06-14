package pt.ipvc.vending.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.ipvc.vending.domain.entity.Cliente;
import pt.ipvc.vending.domain.enums.AuditAction;
import pt.ipvc.vending.repository.ClienteRepository;
import pt.ipvc.vending.repository.ContratoRepository;
import pt.ipvc.vending.repository.PropostaRepository;
import pt.ipvc.vending.service.exception.EntidadeEmUsoException;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ContratoRepository contratoRepository;
    private final PropostaRepository propostaRepository;
    private final AuditLogService auditLogService;
    private final BCryptPasswordEncoder passwordEncoder;

    public ClienteService(ClienteRepository clienteRepository,
                          ContratoRepository contratoRepository,
                          PropostaRepository propostaRepository,
                          AuditLogService auditLogService,
                          BCryptPasswordEncoder passwordEncoder) {
        this.clienteRepository = clienteRepository;
        this.contratoRepository = contratoRepository;
        this.propostaRepository = propostaRepository;
        this.auditLogService = auditLogService;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Cliente> listarTodos() {
        return clienteRepository.findAll();
    }

    public Optional<Cliente> obterPorId(Long id) {
        return clienteRepository.findById(id);
    }

    public Cliente guardar(Cliente form) {
        boolean isNew = form.getId() == null;

        if (isNew) {
            if (form.getPassword() != null && !form.getPassword().isBlank()) {
                form.setPassword(passwordEncoder.encode(form.getPassword()));
            }
            Cliente saved = clienteRepository.save(form);
            auditLogService.logCreate("Cliente", saved.getId(),
                    "Cliente criado: " + saved.getNome() + " (NIF: " + saved.getNif() + ")");
            return saved;
        }

        // For updates, load the stored entity and apply only the changed fields so
        // that username and password are never blindly overwritten by form binding.
        Cliente existing = clienteRepository.findById(form.getId())
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado: " + form.getId()));

        existing.setNome(form.getNome());
        existing.setEmail(form.getEmail());
        existing.setTelefone(form.getTelefone());
        existing.setMorada(form.getMorada());
        existing.setNif(form.getNif());
        existing.setEstado(form.getEstado());
        existing.setDataRegisto(form.getDataRegisto());

        if (form.getUsername() != null && !form.getUsername().isBlank()) {
            existing.setUsername(form.getUsername().trim());
        }

        // Only update the password when a new value was explicitly provided.
        if (form.getPassword() != null && !form.getPassword().isBlank()) {
            existing.setPassword(passwordEncoder.encode(form.getPassword()));
        }

        Cliente saved = clienteRepository.save(existing);
        auditLogService.logUpdate("Cliente", saved.getId(),
                "Cliente atualizado: " + saved.getNome(), null, null);
        return saved;
    }

    public void eliminar(Long id) {
        if (contratoRepository.existsByClienteId(id)) {
            throw new EntidadeEmUsoException(
                    "Cannot delete client because it has active contracts.");
        }
        if (propostaRepository.existsByClienteId(id)) {
            throw new EntidadeEmUsoException(
                    "Cannot delete client because it has active proposals.");
        }
        Cliente c = clienteRepository.findById(id).orElse(null);
        clienteRepository.deleteById(id);
        auditLogService.logDelete("Cliente", id,
                "Cliente eliminado: " + (c != null ? c.getNome() : id));
    }

    /**
     * Lets a logged-in client update their own contact data and, optionally, their password.
     * nome, NIF, estado, username and dataRegisto are never touched.
     *
     * Password change rules:
     *   - All three password fields blank  → password is not changed.
     *   - Any password field non-blank     → full validation is required:
     *       1. passwordAtual must match the stored BCrypt hash.
     *       2. novaPassword and confirmarPassword must be equal.
     *       3. The new password is stored as a BCrypt hash.
     */
    public Cliente atualizarDadosProprios(Long id, String email, String telefone,
                                          String morada, String passwordAtual,
                                          String novaPassword, String confirmarPassword) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado."));

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("O email é obrigatório.");
        }

        boolean passwordChangeRequested =
                (passwordAtual     != null && !passwordAtual.isBlank())  ||
                (novaPassword      != null && !novaPassword.isBlank())   ||
                (confirmarPassword != null && !confirmarPassword.isBlank());

        if (passwordChangeRequested) {
            if (passwordAtual == null || passwordAtual.isBlank()) {
                throw new IllegalArgumentException("A password atual é obrigatória para alterar a password.");
            }
            if (!passwordEncoder.matches(passwordAtual, cliente.getPassword())) {
                throw new IllegalArgumentException("A password atual está incorreta.");
            }
            if (novaPassword == null || novaPassword.isBlank()) {
                throw new IllegalArgumentException("A nova password não pode estar vazia.");
            }
            if (!novaPassword.equals(confirmarPassword)) {
                throw new IllegalArgumentException("As novas passwords não coincidem.");
            }
            cliente.setPassword(passwordEncoder.encode(novaPassword));
        }

        String oldEmail = cliente.getEmail();
        cliente.setEmail(email.trim());
        cliente.setTelefone(telefone != null && !telefone.isBlank() ? telefone.trim() : null);
        cliente.setMorada(morada != null && !morada.isBlank() ? morada.trim() : null);

        Cliente saved = clienteRepository.save(cliente);
        auditLogService.logUpdate("Cliente", saved.getId(),
                "Cliente atualizou os seus dados de contacto" +
                        (passwordChangeRequested ? " e password" : ""),
                "email=" + oldEmail, "email=" + email.trim());
        return saved;
    }
}
