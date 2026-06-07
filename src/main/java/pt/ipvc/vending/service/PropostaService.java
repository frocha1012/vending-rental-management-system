package pt.ipvc.vending.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.ipvc.vending.domain.entity.Contrato;
import pt.ipvc.vending.domain.entity.Instalacao;
import pt.ipvc.vending.domain.entity.Proposta;
import pt.ipvc.vending.domain.enums.EstadoContrato;
import pt.ipvc.vending.domain.enums.EstadoInstalacao;
import pt.ipvc.vending.domain.enums.EstadoProposta;
import pt.ipvc.vending.repository.ContratoRepository;
import pt.ipvc.vending.repository.InstalacaoRepository;
import pt.ipvc.vending.repository.PropostaRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PropostaService {

    private final PropostaRepository propostaRepository;
    private final ContratoRepository contratoRepository;
    private final InstalacaoRepository instalacaoRepository;

    public PropostaService(PropostaRepository propostaRepository,
                           ContratoRepository contratoRepository,
                           InstalacaoRepository instalacaoRepository) {
        this.propostaRepository = propostaRepository;
        this.contratoRepository = contratoRepository;
        this.instalacaoRepository = instalacaoRepository;
    }

    public List<Proposta> listarTodas() {
        return propostaRepository.findAll();
    }

    public List<Proposta> listarTodasComDetalhes() {
        return propostaRepository.findAllWithDetails();
    }

    public Optional<Proposta> obterPorId(Long id) {
        return propostaRepository.findById(id);
    }

    public Proposta guardar(Proposta proposta) {
        return propostaRepository.save(proposta);
    }

    public void eliminar(Long id) {
        propostaRepository.deleteById(id);
    }

    public List<Proposta> listarPorCliente(Long clienteId) {
        return propostaRepository.findByClienteIdWithDetails(clienteId);
    }

    // Manager: set price/notes and mark as EM_ANALISE
    public Proposta tomarEmAnalise(Long id, BigDecimal valorGestor, String observacoesGestor) {
        Proposta proposta = propostaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Proposta nao encontrada: " + id));
        proposta.setValorGestor(valorGestor);
        proposta.setObservacoesGestor(observacoesGestor);
        proposta.setEstado(EstadoProposta.EM_ANALISE);
        return propostaRepository.save(proposta);
    }

    // Manager: send the proposal to the client for review
    public Proposta enviarAoCliente(Long id) {
        Proposta proposta = propostaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Proposta nao encontrada: " + id));
        if (proposta.getValorGestor() == null) {
            throw new IllegalStateException("Cannot send to client without a manager price.");
        }
        proposta.setEstado(EstadoProposta.ENVIADA_CLIENTE);
        return propostaRepository.save(proposta);
    }

    // Client: accept — creates Contrato + Instalacao
    public void aceitar(Long id) {
        Proposta proposta = propostaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Proposta nao encontrada: " + id));

        proposta.setEstado(EstadoProposta.ACEITE);
        propostaRepository.save(proposta);

        BigDecimal valorFinal = proposta.getValorGestor() != null
                ? proposta.getValorGestor()
                : proposta.getValorProposto();

        int anos = proposta.getDuracaoAnos() != null ? proposta.getDuracaoAnos() : 1;

        Contrato contrato = new Contrato();
        contrato.setCliente(proposta.getCliente());
        contrato.setVendingMachine(proposta.getVendingMachine());
        contrato.setDataInicio(LocalDate.now());
        contrato.setDataFim(LocalDate.now().plusYears(anos));
        contrato.setValorMensal(valorFinal);
        contrato.setEstado(EstadoContrato.ATIVO);
        contrato = contratoRepository.save(contrato);

        Instalacao instalacao = new Instalacao();
        instalacao.setContrato(contrato);
        instalacao.setDataInstalacao(LocalDate.now().plusDays(7));
        instalacao.setLocalInstalacao("A definir");
        instalacao.setEstado(EstadoInstalacao.AGENDADA);
        instalacao.setObservacoes("Instalacao criada automaticamente apos aceitacao de proposta #" + id);
        instalacaoRepository.save(instalacao);
    }

    // Client: reject
    public void rejeitar(Long id) {
        Proposta proposta = propostaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Proposta nao encontrada: " + id));
        proposta.setEstado(EstadoProposta.REJEITADA);
        propostaRepository.save(proposta);
    }

    // Client: counter-propose with a new value and optionally a new duration
    public void contraproposta(Long id, BigDecimal novoValor, String observacoes, Integer novaDuracao) {
        Proposta proposta = propostaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Proposta nao encontrada: " + id));
        proposta.setValorProposto(novoValor);
        proposta.setObservacoes(observacoes);
        if (novaDuracao != null) {
            proposta.setDuracaoAnos(novaDuracao);
        }
        proposta.setEstado(EstadoProposta.CONTRAPROPOSTA);
        propostaRepository.save(proposta);
    }
}
