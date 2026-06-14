package pt.ipvc.vending.web.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pt.ipvc.vending.service.AccountRequestService;

@Controller
public class RegisterController {

    private final AccountRequestService accountRequestService;

    public RegisterController(AccountRequestService accountRequestService) {
        this.accountRequestService = accountRequestService;
    }

    @GetMapping("/register")
    public String registerPage(HttpSession session) {
        if (session.getAttribute("clienteId") != null) {
            return "redirect:/portal";
        }
        return "register";
    }

    @PostMapping("/register")
    public String registerSubmit(
            @RequestParam String nome,
            @RequestParam String nif,
            @RequestParam String email,
            @RequestParam(required = false) String telefone,
            @RequestParam(required = false) String morada,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            Model model) {

        try {
            accountRequestService.submeter(nome, nif, email, telefone, morada,
                    username, password, confirmPassword);
            model.addAttribute("sucesso", true);
            return "register";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("erro", ex.getMessage());
            // Re-populate safe fields so the user does not have to retype everything
            model.addAttribute("nome", nome);
            model.addAttribute("nif", nif);
            model.addAttribute("email", email);
            model.addAttribute("telefone", telefone);
            model.addAttribute("morada", morada);
            model.addAttribute("username", username);
            return "register";
        }
    }
}
