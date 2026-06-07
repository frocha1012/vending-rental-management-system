package pt.ipvc.vending.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pt.ipvc.vending.domain.entity.Cliente;
import pt.ipvc.vending.domain.enums.EstadoCliente;
import pt.ipvc.vending.service.ClienteService;
import pt.ipvc.vending.service.exception.EntidadeEmUsoException;

@Controller
@RequestMapping("/clientes")
public class ClienteWebController {

    private final ClienteService clienteService;

    public ClienteWebController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("clientes", clienteService.listarTodos());
        return "clientes/list";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("cliente", new Cliente());
        model.addAttribute("estados", EstadoCliente.values());
        return "clientes/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Cliente cliente = clienteService.obterPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente nao encontrado: " + id));
        model.addAttribute("cliente", cliente);
        model.addAttribute("estados", EstadoCliente.values());
        return "clientes/form";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Cliente cliente, RedirectAttributes redirectAttributes) {
        clienteService.guardar(cliente);
        redirectAttributes.addFlashAttribute("sucesso", "Client saved successfully.");
        return "redirect:/clientes";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            clienteService.eliminar(id);
            redirectAttributes.addFlashAttribute("sucesso", "Client deleted successfully.");
        } catch (EntidadeEmUsoException ex) {
            redirectAttributes.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/clientes";
    }
}
