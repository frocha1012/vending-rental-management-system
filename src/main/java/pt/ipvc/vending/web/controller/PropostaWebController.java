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
import pt.ipvc.vending.domain.entity.Proposta;
import pt.ipvc.vending.domain.entity.VendingMachine;
import pt.ipvc.vending.domain.enums.EstadoProposta;
import pt.ipvc.vending.service.ClienteService;
import pt.ipvc.vending.service.PropostaService;
import pt.ipvc.vending.service.VendingMachineService;

@Controller
@RequestMapping("/propostas")
public class PropostaWebController {

    private final PropostaService propostaService;
    private final ClienteService clienteService;
    private final VendingMachineService vendingMachineService;

    public PropostaWebController(PropostaService propostaService,
                                 ClienteService clienteService,
                                 VendingMachineService vendingMachineService) {
        this.propostaService = propostaService;
        this.clienteService = clienteService;
        this.vendingMachineService = vendingMachineService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("propostas", propostaService.listarTodas());
        return "propostas/list";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        Proposta proposta = new Proposta();
        proposta.setCliente(new Cliente());
        proposta.setVendingMachine(new VendingMachine());
        preencherFormulario(model, proposta);
        return "propostas/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Proposta proposta = propostaService.obterPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Proposta nao encontrada: " + id));
        preencherFormulario(model, proposta);
        return "propostas/form";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Proposta proposta, RedirectAttributes redirectAttributes) {
        proposta.setCliente(clienteService.obterPorId(proposta.getCliente().getId())
                .orElseThrow(() -> new IllegalArgumentException("Cliente invalido")));
        proposta.setVendingMachine(vendingMachineService.obterPorId(proposta.getVendingMachine().getId())
                .orElseThrow(() -> new IllegalArgumentException("Vending machine invalida")));
        propostaService.guardar(proposta);
        redirectAttributes.addFlashAttribute("sucesso", "Proposal saved successfully.");
        return "redirect:/propostas";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        propostaService.eliminar(id);
        redirectAttributes.addFlashAttribute("sucesso", "Proposal deleted successfully.");
        return "redirect:/propostas";
    }

    private void preencherFormulario(Model model, Proposta proposta) {
        model.addAttribute("proposta", proposta);
        model.addAttribute("clientes", clienteService.listarTodos());
        model.addAttribute("vendingMachines", vendingMachineService.listarTodas());
        model.addAttribute("estados", EstadoProposta.values());
    }
}
