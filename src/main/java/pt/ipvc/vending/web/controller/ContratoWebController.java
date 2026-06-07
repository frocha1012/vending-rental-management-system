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
import pt.ipvc.vending.domain.entity.Contrato;
import pt.ipvc.vending.domain.entity.VendingMachine;
import pt.ipvc.vending.domain.enums.EstadoContrato;
import pt.ipvc.vending.service.ClienteService;
import pt.ipvc.vending.service.ContratoService;
import pt.ipvc.vending.service.VendingMachineService;
import pt.ipvc.vending.service.exception.EntidadeEmUsoException;

@Controller
@RequestMapping("/contratos")
public class ContratoWebController {

    private final ContratoService contratoService;
    private final ClienteService clienteService;
    private final VendingMachineService vendingMachineService;

    public ContratoWebController(ContratoService contratoService,
                                 ClienteService clienteService,
                                 VendingMachineService vendingMachineService) {
        this.contratoService = contratoService;
        this.clienteService = clienteService;
        this.vendingMachineService = vendingMachineService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("contratos", contratoService.listarTodos());
        return "contratos/list";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        Contrato contrato = new Contrato();
        contrato.setCliente(new Cliente());
        contrato.setVendingMachine(new VendingMachine());
        preencherFormulario(model, contrato);
        return "contratos/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Contrato contrato = contratoService.obterPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Contrato nao encontrado: " + id));
        preencherFormulario(model, contrato);
        return "contratos/form";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Contrato contrato, RedirectAttributes redirectAttributes) {
        contrato.setCliente(clienteService.obterPorId(contrato.getCliente().getId())
                .orElseThrow(() -> new IllegalArgumentException("Cliente invalido")));
        contrato.setVendingMachine(vendingMachineService.obterPorId(contrato.getVendingMachine().getId())
                .orElseThrow(() -> new IllegalArgumentException("Vending machine invalida")));
        contratoService.guardar(contrato);
        redirectAttributes.addFlashAttribute("sucesso", "Contract saved successfully.");
        return "redirect:/contratos";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            contratoService.eliminar(id);
            redirectAttributes.addFlashAttribute("sucesso", "Contract deleted successfully.");
        } catch (EntidadeEmUsoException ex) {
            redirectAttributes.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/contratos";
    }

    private void preencherFormulario(Model model, Contrato contrato) {
        model.addAttribute("contrato", contrato);
        model.addAttribute("clientes", clienteService.listarTodos());
        model.addAttribute("vendingMachines", vendingMachineService.listarTodas());
        model.addAttribute("estados", EstadoContrato.values());
    }
}
