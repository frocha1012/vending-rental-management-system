package pt.ipvc.vending.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pt.ipvc.vending.domain.entity.Contrato;
import pt.ipvc.vending.domain.entity.Instalacao;
import pt.ipvc.vending.domain.enums.EstadoInstalacao;
import pt.ipvc.vending.service.ContratoService;
import pt.ipvc.vending.service.InstalacaoService;

@Controller
@RequestMapping("/instalacoes")
public class InstalacaoWebController {

    private final InstalacaoService instalacaoService;
    private final ContratoService contratoService;

    public InstalacaoWebController(InstalacaoService instalacaoService,
                                   ContratoService contratoService) {
        this.instalacaoService = instalacaoService;
        this.contratoService = contratoService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("instalacoes", instalacaoService.listarTodasComDetalhes());
        return "instalacoes/list";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        Instalacao instalacao = new Instalacao();
        instalacao.setContrato(new Contrato());
        preencherFormulario(model, instalacao);
        return "instalacoes/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Instalacao instalacao = instalacaoService.obterPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Instalacao nao encontrada: " + id));
        preencherFormulario(model, instalacao);
        return "instalacoes/form";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Instalacao instalacao, RedirectAttributes redirectAttributes) {
        instalacao.setContrato(contratoService.obterPorId(instalacao.getContrato().getId())
                .orElseThrow(() -> new IllegalArgumentException("Contrato invalido")));
        instalacaoService.guardar(instalacao);
        redirectAttributes.addFlashAttribute("sucesso", "Installation saved successfully.");
        return "redirect:/instalacoes";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        instalacaoService.eliminar(id);
        redirectAttributes.addFlashAttribute("sucesso", "Installation deleted successfully.");
        return "redirect:/instalacoes";
    }

    private void preencherFormulario(Model model, Instalacao instalacao) {
        model.addAttribute("instalacao", instalacao);
        model.addAttribute("contratos", contratoService.listarTodosComDetalhes());
        model.addAttribute("estados", EstadoInstalacao.values());
    }
}
