package pt.ipvc.vending.web.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pt.ipvc.vending.domain.entity.PasswordResetToken;
import pt.ipvc.vending.service.PasswordResetService;

import java.util.Optional;

@Controller
public class ForgotPasswordController {

    private final PasswordResetService passwordResetService;

    public ForgotPasswordController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    // ── Forgot password ───────────────────────────────────────────────────────

    @GetMapping("/forgot-password")
    public String forgotPasswordPage(HttpSession session) {
        if (session.getAttribute("clienteId") != null) {
            return "redirect:/portal";
        }
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPasswordSubmit(@RequestParam(required = false) String username,
                                       @RequestParam(required = false) String email,
                                       Model model) {
        // Always process silently and show the same generic message
        passwordResetService.requestReset(username, email);
        model.addAttribute("enviado", true);
        return "forgot-password";
    }

    // ── Reset password ────────────────────────────────────────────────────────

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam(required = false) String token,
                                    HttpSession session,
                                    Model model) {
        if (session.getAttribute("clienteId") != null) {
            return "redirect:/portal";
        }

        if (token == null || token.isBlank()) {
            model.addAttribute("erroToken", "Link inválido. Solicite um novo.");
            return "reset-password";
        }

        Optional<PasswordResetToken> opt = passwordResetService.validateToken(token);
        if (opt.isEmpty()) {
            model.addAttribute("erroToken", "Este link é inválido ou já expirou. Solicite um novo.");
            return "reset-password";
        }

        model.addAttribute("token", token);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPasswordSubmit(@RequestParam(required = false) String token,
                                      @RequestParam(required = false) String newPassword,
                                      @RequestParam(required = false) String confirmPassword,
                                      Model model) {
        try {
            passwordResetService.completeReset(token, newPassword, confirmPassword);
            return "redirect:/login?resetSuccess";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("erro", ex.getMessage());
            // Only pass the token back if it is still potentially valid
            if (token != null && passwordResetService.validateToken(token).isPresent()) {
                model.addAttribute("token", token);
            }
            return "reset-password";
        }
    }
}
