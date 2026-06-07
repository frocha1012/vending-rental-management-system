package pt.ipvc.vending.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pt.ipvc.vending.domain.entity.VendingMachine;
import pt.ipvc.vending.domain.enums.EstadoVendingMachine;
import pt.ipvc.vending.service.VendingMachineService;
import pt.ipvc.vending.service.exception.EntidadeEmUsoException;

@Controller
@RequestMapping("/vending-machines")
public class VendingMachineWebController {

    private final VendingMachineService vendingMachineService;

    public VendingMachineWebController(VendingMachineService vendingMachineService) {
        this.vendingMachineService = vendingMachineService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("vendingMachines", vendingMachineService.listarTodas());
        return "vending-machines/list";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("vendingMachine", new VendingMachine());
        model.addAttribute("estados", EstadoVendingMachine.values());
        return "vending-machines/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        VendingMachine vendingMachine = vendingMachineService.obterPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Vending machine nao encontrada: " + id));
        model.addAttribute("vendingMachine", vendingMachine);
        model.addAttribute("estados", EstadoVendingMachine.values());
        return "vending-machines/form";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute VendingMachine vendingMachine, RedirectAttributes redirectAttributes) {
        vendingMachineService.guardar(vendingMachine);
        redirectAttributes.addFlashAttribute("sucesso", "Vending machine saved successfully.");
        return "redirect:/vending-machines";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            vendingMachineService.eliminar(id);
            redirectAttributes.addFlashAttribute("sucesso", "Vending machine deleted successfully.");
        } catch (EntidadeEmUsoException ex) {
            redirectAttributes.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/vending-machines";
    }
}
