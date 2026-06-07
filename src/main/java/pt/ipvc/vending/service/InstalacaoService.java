package pt.ipvc.vending.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.ipvc.vending.domain.entity.Instalacao;
import pt.ipvc.vending.domain.enums.EstadoInstalacao;
import pt.ipvc.vending.domain.enums.MotivoAdiamento;
import pt.ipvc.vending.repository.InstalacaoRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class InstalacaoService {

    private final InstalacaoRepository instalacaoRepository;

    public InstalacaoService(InstalacaoRepository instalacaoRepository) {
        this.instalacaoRepository = instalacaoRepository;
    }

    public List<Instalacao> listarTodas() {
        return instalacaoRepository.findAll();
    }

    public List<Instalacao> listarTodasComDetalhes() {
        return instalacaoRepository.findAllWithDetails();
    }

    public List<Instalacao> listarPorCliente(Long clienteId) {
        return instalacaoRepository.findByClienteIdWithDetails(clienteId);
    }

    public Optional<Instalacao> obterPorId(Long id) {
        return instalacaoRepository.findById(id);
    }

    public Instalacao guardar(Instalacao instalacao) {
        return instalacaoRepository.save(instalacao);
    }

    public void eliminar(Long id) {
        instalacaoRepository.deleteById(id);
    }

    /** Técnico marks an installation as completed; sets dataConclusao to today. */
    public void concluir(Long id) {
        Instalacao inst = instalacaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Instalação não encontrada: " + id));
        if (inst.getEstado() != EstadoInstalacao.AGENDADA) {
            throw new IllegalStateException("Só é possível concluir instalações no estado AGENDADA.");
        }
        inst.setEstado(EstadoInstalacao.CONCLUIDA);
        inst.setDataConclusao(LocalDate.now());
        instalacaoRepository.save(inst);
    }

    /** Técnico postpones an installation: sets new date and delay reason. */
    public void adiar(Long id, LocalDate novaData, MotivoAdiamento motivo) {
        Instalacao inst = instalacaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Instalação não encontrada: " + id));
        if (inst.getEstado() != EstadoInstalacao.AGENDADA) {
            throw new IllegalStateException("Só é possível adiar instalações no estado AGENDADA.");
        }
        if (novaData == null || !novaData.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("A nova data deve ser uma data futura.");
        }
        inst.setEstado(EstadoInstalacao.ADIADA);
        inst.setNovaDataAgendada(novaData);
        inst.setMotivoAdiamento(motivo);
        instalacaoRepository.save(inst);
    }
}
