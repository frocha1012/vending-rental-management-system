package pt.ipvc.vending.web.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pt.ipvc.vending.domain.entity.Cliente;
import pt.ipvc.vending.domain.entity.Contrato;
import pt.ipvc.vending.domain.entity.PedidoRescisaoContrato;
import pt.ipvc.vending.domain.entity.Proposta;
import pt.ipvc.vending.domain.enums.EstadoContrato;
import pt.ipvc.vending.domain.enums.EstadoProposta;
import pt.ipvc.vending.domain.enums.MotivoRescisao;
import pt.ipvc.vending.service.ClienteService;
import pt.ipvc.vending.service.ContratoService;
import pt.ipvc.vending.service.InstalacaoService;
import pt.ipvc.vending.service.PedidoRescisaoContratoService;
import pt.ipvc.vending.service.PropostaService;
import pt.ipvc.vending.service.VendingMachineService;
import pt.ipvc.vending.service.exception.EntidadeEmUsoException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/portal")
public class PortalController {

    private final ClienteService clienteService;
    private final ContratoService contratoService;
    private final PropostaService propostaService;
    private final InstalacaoService instalacaoService;
    private final VendingMachineService vendingMachineService;
    private final PedidoRescisaoContratoService rescisaoService;

    public PortalController(ClienteService clienteService,
                            ContratoService contratoService,
                            PropostaService propostaService,
                            InstalacaoService instalacaoService,
                            VendingMachineService vendingMachineService,
                            PedidoRescisaoContratoService rescisaoService) {
        this.clienteService = clienteService;
        this.contratoService = contratoService;
        this.propostaService = propostaService;
        this.instalacaoService = instalacaoService;
        this.vendingMachineService = vendingMachineService;
        this.rescisaoService = rescisaoService;
    }

    // ── Dashboard ──────────────────────────────────────────────────────────────

    @GetMapping
    public String home(HttpSession session, Model model) {
        Cliente cliente = clienteAtual(session);
        List<Contrato> contratos     = contratoService.listarPorCliente(cliente.getId());
        List<Proposta> propostas     = propostaService.listarPorCliente(cliente.getId());

        long contratosAtivos = contratos.stream()
                .filter(c -> c.getEstado() == EstadoContrato.ATIVO).count();

        // Most recent 3 of each (lists come sorted by DB insertion order; take last)
        List<Proposta> recentePropostas = propostas.stream()
                .sorted((a, b) -> Long.compare(b.getId(), a.getId()))
                .limit(3).collect(Collectors.toList());
        List<Contrato> recenteContratos = contratos.stream()
                .sorted((a, b) -> Long.compare(b.getId(), a.getId()))
                .limit(3).collect(Collectors.toList());

        model.addAttribute("cliente", cliente);
        model.addAttribute("totalContratos", contratosAtivos);
        model.addAttribute("totalPropostas", propostas.size());
        model.addAttribute("totalInstalacoes", instalacaoService.listarPorCliente(cliente.getId()).size());
        model.addAttribute("recentePropostas", recentePropostas);
        model.addAttribute("recenteContratos", recenteContratos);
        return "portal/index";
    }

    // ── My data ────────────────────────────────────────────────────────────────

    @GetMapping("/dados")
    public String dados(HttpSession session, Model model) {
        model.addAttribute("cliente", clienteAtual(session));
        return "portal/dados";
    }

    @PostMapping("/dados/editar")
    public String editarDados(HttpSession session,
                              @RequestParam String email,
                              @RequestParam(required = false) String telefone,
                              @RequestParam(required = false) String morada,
                              @RequestParam(required = false) String novaPassword,
                              RedirectAttributes redirectAttributes) {
        Long clienteId = (Long) session.getAttribute("clienteId");
        try {
            clienteService.atualizarDadosProprios(clienteId, email, telefone, morada, novaPassword);
            redirectAttributes.addFlashAttribute("sucesso",
                    "Os seus dados foram atualizados com sucesso.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("erro", ex.getMessage());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("erro",
                    "Ocorreu um erro ao guardar os dados. Tente novamente.");
        }
        return "redirect:/portal/dados";
    }

    // ── Contracts / Installations (read-only) ──────────────────────────────────

    @GetMapping("/contratos")
    public String contratos(HttpSession session, Model model) {
        Cliente cliente = clienteAtual(session);
        List<Contrato> contratos = contratoService.listarPorCliente(cliente.getId());

        List<Long> contratoIds = contratos.stream().map(Contrato::getId).collect(Collectors.toList());
        Set<Long> comPedidoPendente = rescisaoService.contratosComPedidoPendente(contratoIds);

        model.addAttribute("cliente", cliente);
        model.addAttribute("contratos", contratos);
        model.addAttribute("comPedidoPendente", comPedidoPendente);
        model.addAttribute("ATIVO", EstadoContrato.ATIVO);
        return "portal/contratos";
    }

    // ── Contract termination request ────────────────────────────────────────────

    @GetMapping("/contratos/{contratoId}/rescisao")
    public String rescisaoForm(@PathVariable Long contratoId,
                               HttpSession session, Model model,
                               RedirectAttributes redirectAttributes) {
        Contrato contrato = validarPropriedadeContrato(contratoId, session);
        Long clienteId = (Long) session.getAttribute("clienteId");
        List<Long> ids = List.of(contratoId);
        if (!rescisaoService.contratosComPedidoPendente(ids).isEmpty()) {
            redirectAttributes.addFlashAttribute("erro",
                    "Já existe um pedido de rescisão pendente para este contrato.");
            return "redirect:/portal/contratos";
        }
        if (contrato.getEstado() != EstadoContrato.ATIVO) {
            redirectAttributes.addFlashAttribute("erro",
                    "Só é possível pedir rescisão de contratos ativos.");
            return "redirect:/portal/contratos";
        }
        model.addAttribute("contrato", contrato);
        model.addAttribute("motivos", MotivoRescisao.values());
        return "portal/rescisao-form";
    }

    @PostMapping("/contratos/{contratoId}/rescisao")
    public String rescisaoSubmit(@PathVariable Long contratoId,
                                 HttpSession session,
                                 @RequestParam String motivo,
                                 @RequestParam(required = false) String descricao,
                                 RedirectAttributes redirectAttributes) {
        Contrato contrato = validarPropriedadeContrato(contratoId, session);

        PedidoRescisaoContrato pedido = new PedidoRescisaoContrato();
        pedido.setContrato(contrato);
        pedido.setMotivo(MotivoRescisao.valueOf(motivo));
        pedido.setDescricao(descricao);
        pedido.setDataPedido(LocalDate.now());

        try {
            rescisaoService.submeter(pedido);
            redirectAttributes.addFlashAttribute("sucesso",
                    "Pedido de rescisão submetido. Aguarde aprovação do gestor.");
        } catch (EntidadeEmUsoException ex) {
            redirectAttributes.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/portal/contratos";
    }

    @GetMapping("/instalacoes")
    public String instalacoes(HttpSession session, Model model) {
        Cliente cliente = clienteAtual(session);
        model.addAttribute("cliente", cliente);
        model.addAttribute("instalacoes", instalacaoService.listarPorCliente(cliente.getId()));
        return "portal/instalacoes";
    }

    // ── Proposals list ─────────────────────────────────────────────────────────

    @GetMapping("/propostas")
    public String propostas(HttpSession session, Model model) {
        Cliente cliente = clienteAtual(session);
        model.addAttribute("cliente", cliente);
        model.addAttribute("propostas", propostaService.listarPorCliente(cliente.getId()));
        model.addAttribute("ENVIADA_CLIENTE", EstadoProposta.ENVIADA_CLIENTE);
        model.addAttribute("ACEITE", EstadoProposta.ACEITE);
        model.addAttribute("REJEITADA", EstadoProposta.REJEITADA);
        return "portal/propostas";
    }

    // ── New proposal form ──────────────────────────────────────────────────────

    @GetMapping("/propostas/nova")
    public String novaProposta(Model model) {
        model.addAttribute("vendingMachines", vendingMachineService.listarTodas());
        return "portal/nova-proposta";
    }

    @PostMapping("/propostas/submeter")
    public String submeterProposta(HttpSession session,
                                   @RequestParam Long vendingMachineId,
                                   @RequestParam BigDecimal valorProposto,
                                   @RequestParam Integer duracaoAnos,
                                   @RequestParam(required = false) String observacoes,
                                   RedirectAttributes redirectAttributes) {
        Cliente cliente = clienteAtual(session);

        Proposta proposta = new Proposta();
        proposta.setCliente(cliente);
        proposta.setVendingMachine(vendingMachineService.obterPorId(vendingMachineId)
                .orElseThrow(() -> new IllegalArgumentException("Vending machine invalida")));
        proposta.setValorProposto(valorProposto);
        proposta.setDuracaoAnos(duracaoAnos);
        proposta.setObservacoes(observacoes);
        proposta.setDataProposta(LocalDate.now());
        proposta.setEstado(EstadoProposta.PENDENTE);
        propostaService.guardar(proposta);

        redirectAttributes.addFlashAttribute("sucesso", "Proposta submetida com sucesso. Aguarde resposta do gestor.");
        return "redirect:/portal/propostas";
    }

    // ── Client negotiation actions ─────────────────────────────────────────────

    @PostMapping("/propostas/{id}/aceitar")
    public String aceitar(@PathVariable Long id,
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {
        validarPropriedadeProposta(id, session);
        propostaService.aceitar(id);
        redirectAttributes.addFlashAttribute("sucesso",
                "Proposta aceite! O contrato e a instalação foram criados automaticamente.");
        return "redirect:/portal/propostas";
    }

    @PostMapping("/propostas/{id}/rejeitar")
    public String rejeitar(@PathVariable Long id,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        validarPropriedadeProposta(id, session);
        propostaService.rejeitar(id);
        redirectAttributes.addFlashAttribute("sucesso", "Proposta rejeitada.");
        return "redirect:/portal/propostas";
    }

    @GetMapping("/propostas/{id}/contraproposta")
    public String contrapropostaForm(@PathVariable Long id, HttpSession session, Model model) {
        Proposta proposta = validarPropriedadeProposta(id, session);
        model.addAttribute("proposta", proposta);
        return "portal/contraproposta-form";
    }

    @PostMapping("/propostas/{id}/contraproposta")
    public String contrapropostaSubmit(@PathVariable Long id,
                                       HttpSession session,
                                       @RequestParam BigDecimal novoValor,
                                       @RequestParam(required = false) Integer novaDuracao,
                                       @RequestParam(required = false) String observacoes,
                                       RedirectAttributes redirectAttributes) {
        validarPropriedadeProposta(id, session);
        propostaService.contraproposta(id, novoValor, observacoes, novaDuracao);
        redirectAttributes.addFlashAttribute("sucesso",
                "Contraproposta enviada. O gestor irá rever os novos termos.");
        return "redirect:/portal/propostas";
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private Cliente clienteAtual(HttpSession session) {
        Long clienteId = (Long) session.getAttribute("clienteId");
        return clienteService.obterPorId(clienteId)
                .orElseThrow(() -> new IllegalStateException("Session invalid"));
    }

    private Proposta validarPropriedadeProposta(Long propostaId, HttpSession session) {
        Proposta proposta = propostaService.obterPorId(propostaId)
                .orElseThrow(() -> new IllegalArgumentException("Proposta nao encontrada"));
        Long clienteId = (Long) session.getAttribute("clienteId");
        if (!proposta.getCliente().getId().equals(clienteId)) {
            throw new IllegalStateException("Access denied");
        }
        return proposta;
    }

    private Contrato validarPropriedadeContrato(Long contratoId, HttpSession session) {
        Long clienteId = (Long) session.getAttribute("clienteId");
        return contratoService.listarPorCliente(clienteId).stream()
                .filter(c -> c.getId().equals(contratoId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Contrato nao encontrado ou acesso negado"));
    }
}
