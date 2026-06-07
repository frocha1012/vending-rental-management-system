package pt.ipvc.vending.web.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(HttpSession session) {
        if (session.getAttribute("clienteId") != null) {
            return "redirect:/portal";
        }
        return "redirect:/login";
    }
}
