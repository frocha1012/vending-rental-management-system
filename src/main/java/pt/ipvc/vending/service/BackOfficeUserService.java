package pt.ipvc.vending.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.ipvc.vending.domain.entity.BackOfficeUser;
import pt.ipvc.vending.domain.enums.AuditAction;
import pt.ipvc.vending.domain.enums.BackOfficeRole;
import pt.ipvc.vending.repository.BackOfficeUserRepository;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BackOfficeUserService {

    private final BackOfficeUserRepository repo;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public BackOfficeUserService(BackOfficeUserRepository repo,
                                 BCryptPasswordEncoder passwordEncoder,
                                 AuditLogService auditLogService) {
        this.repo            = repo;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
    }

    // ── Authentication ────────────────────────────────────────────────────────

    /**
     * Verifies credentials and returns the user on success.
     * Throws a descriptive {@link IllegalArgumentException} on any failure so
     * callers can display it directly to the user.
     * Every outcome is written to the audit log.
     */
    @Transactional
    public BackOfficeUser authenticate(String username, String rawPassword) {
        Optional<BackOfficeUser> opt = repo.findByUsername(username);

        if (opt.isEmpty()) {
            AuditContext.setActor("SYSTEM", "System");
            auditLogService.logCustomAction(AuditAction.LOGIN_FAILED, "BackOfficeUser", null,
                    "Login falhado: utilizador desconhecido '" + username + "'");
            throw new IllegalArgumentException("Credenciais inválidas.");
        }

        BackOfficeUser user = opt.get();

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            AuditContext.setActor("SYSTEM", "System");
            auditLogService.logCustomAction(AuditAction.LOGIN_FAILED, "BackOfficeUser", user.getId(),
                    "Login falhado: password incorreta para '" + username + "'");
            throw new IllegalArgumentException("Credenciais inválidas.");
        }

        if (!user.isActive()) {
            AuditContext.setActor("SYSTEM", "System");
            auditLogService.logCustomAction(AuditAction.LOGIN_FAILED, "BackOfficeUser", user.getId(),
                    "Login falhado: conta inativa para '" + username + "'");
            throw new IllegalArgumentException("Conta desativada. Contacte o administrador.");
        }

        AuditContext.setActor(user.getRole().name(), user.getUsername());
        auditLogService.logCustomAction(AuditAction.LOGIN, "BackOfficeUser", user.getId(),
                "Login BackOffice: " + user.getUsername() + " [" + user.getRole().getLabel() + "]");

        return user;
    }

    // ── Queries ────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<BackOfficeUser> listarTodos() {
        return repo.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<BackOfficeUser> obterPorId(Long id) {
        return repo.findById(id);
    }

    // ── Admin: create user ─────────────────────────────────────────────────────

    /**
     * Creates a new BackOffice user. Only ADMIN can call this, and the target
     * role must not be ADMIN.
     */
    public BackOfficeUser criar(String username, String rawPassword, BackOfficeRole role) {
        if (role == BackOfficeRole.ADMIN) {
            throw new IllegalArgumentException("Não é permitido criar utilizadores com a função ADMIN.");
        }
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("O username é obrigatório.");
        }
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("A password é obrigatória.");
        }
        if (repo.existsByUsername(username.trim())) {
            throw new IllegalArgumentException("Já existe um utilizador com o username '" + username.trim() + "'.");
        }

        BackOfficeUser user = new BackOfficeUser();
        user.setUsername(username.trim());
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        user.setActive(true);

        BackOfficeUser saved = repo.save(user);
        auditLogService.logCreate("BackOfficeUser", saved.getId(),
                "Utilizador BackOffice criado: " + saved.getUsername()
                        + " [" + saved.getRole().getLabel() + "]");
        return saved;
    }

    // ── Admin: toggle active ───────────────────────────────────────────────────

    public BackOfficeUser toggleActive(Long id) {
        BackOfficeUser user = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Utilizador não encontrado."));

        if (user.getRole() == BackOfficeRole.ADMIN) {
            throw new IllegalArgumentException("Não é possível desativar uma conta de ADMIN.");
        }

        boolean novoEstado = !user.isActive();
        user.setActive(novoEstado);
        BackOfficeUser saved = repo.save(user);

        auditLogService.logStatusChange("BackOfficeUser", saved.getId(),
                "Utilizador BackOffice " + (novoEstado ? "ativado" : "desativado")
                        + ": " + saved.getUsername(),
                String.valueOf(!novoEstado), String.valueOf(novoEstado));
        return saved;
    }

    // ── Admin: reset password ─────────────────────────────────────────────────

    public void resetPassword(Long id, String novaPassword) {
        if (novaPassword == null || novaPassword.isBlank()) {
            throw new IllegalArgumentException("A nova password não pode estar vazia.");
        }
        BackOfficeUser user = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Utilizador não encontrado."));

        user.setPasswordHash(passwordEncoder.encode(novaPassword));
        repo.save(user);

        auditLogService.logCustomAction(AuditAction.UPDATE, "BackOfficeUser", user.getId(),
                "Password de BackOffice reposta para: " + user.getUsername());
    }

    // ── Internal: seed without audit noise ────────────────────────────────────

    public void seedIfAbsent(String username, String rawPassword, BackOfficeRole role) {
        if (!repo.existsByUsername(username)) {
            BackOfficeUser user = new BackOfficeUser();
            user.setUsername(username);
            user.setPasswordHash(passwordEncoder.encode(rawPassword));
            user.setRole(role);
            user.setActive(true);
            repo.save(user);
        }
    }
}
