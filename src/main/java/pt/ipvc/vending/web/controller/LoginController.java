package pt.ipvc.vending.web.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pt.ipvc.vending.domain.entity.Cliente;
import pt.ipvc.vending.repository.ClienteRepository;

@Controller
public class LoginController {

    private final ClienteRepository clienteRepository;

    public LoginController(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
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
        Cliente cliente = clienteRepository.findByUsernameAndPassword(username, password)
                .orElse(null);

        if (cliente == null) {
            model.addAttribute("erro", "Invalid username or password.");
            return "login";
        }

        session.setAttribute("clienteId", cliente.getId());
        session.setAttribute("clienteNome", cliente.getNome());
        return "redirect:/portal";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
