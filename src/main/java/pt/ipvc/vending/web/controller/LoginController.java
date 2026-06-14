package pt.ipvc.vending.web.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pt.ipvc.vending.domain.entity.Cliente;
import pt.ipvc.vending.domain.enums.AuditAction;
import pt.ipvc.vending.repository.ClienteRepository;
import pt.ipvc.vending.service.AuditContext;
import pt.ipvc.vending.service.AuditLogService;

@Controller
public class LoginController {

    private final ClienteRepository clienteRepository;
    private final AuditLogService auditLogService;
    private final BCryptPasswordEncoder passwordEncoder;

    public LoginController(ClienteRepository clienteRepository,
                           AuditLogService auditLogService,
                           BCryptPasswordEncoder passwordEncoder) {
        this.clienteRepository = clienteRepository;
        this.auditLogService = auditLogService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String loginPage(HttpSession session) {
        if (session.getAttribute("clienteId") != null) {
            return "redirect:/portal";
        }
        return "login";
    }

    @PostMapping("/login")
    public String loginSubmit(@RequestParam String username,
                              @RequestParam String password,
                              HttpSession session,
                              Model model) {
        Cliente cliente = clienteRepository.findByUsername(username).orElse(null);

        if (cliente == null || !passwordEncoder.matches(password, cliente.getPassword())) {
            model.addAttribute("erro", "Credenciais inválidas.");
            return "login";
        }

        session.setAttribute("clienteId", cliente.getId());
        session.setAttribute("clienteNome", cliente.getNome());

        AuditContext.setActor("CLIENTE", cliente.getNome());
        auditLogService.logCustomAction(AuditAction.LOGIN, "Cliente", cliente.getId(),
                "Login efetuado pelo cliente: " + cliente.getUsername());

        return "redirect:/portal";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        Long clienteId = (Long) session.getAttribute("clienteId");
        String clienteNome = (String) session.getAttribute("clienteNome");

        if (clienteId != null) {
            AuditContext.setActor("CLIENTE", clienteNome != null ? clienteNome : "?");
            auditLogService.logCustomAction(AuditAction.LOGOUT, "Cliente", clienteId,
                    "Logout do cliente: " + clienteNome);
        }

        session.invalidate();
        return "redirect:/login";
    }
}
