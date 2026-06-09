package pt.ipvc.vending.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.ipvc.vending.domain.entity.Instalacao;
import pt.ipvc.vending.domain.enums.AuditAction;
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
    private final AuditLogService auditLogService;

    public InstalacaoService(InstalacaoRepository instalacaoRepository,
                             AuditLogService auditLogService) {
        this.instalacaoRepository = instalacaoRepository;
        this.auditLogService = auditLogService;
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
        boolean isNew = instalacao.getId() == null;
        Instalacao saved = instalacaoRepository.save(instalacao);
        if (isNew) {
            auditLogService.logCreate("Instalacao", saved.getId(),
                    "Instalação criada — estado: " + saved.getEstado()
                    + ", local: " + saved.getLocalInstalacao());
        } else {
            auditLogService.logUpdate("Instalacao", saved.getId(),
                    "Instalação atualizada — estado: " + saved.getEstado(), null, null);
        }
        return saved;
    }

    public void eliminar(Long id) {
        instalacaoRepository.deleteById(id);
        auditLogService.logDelete("Instalacao", id, "Instalação eliminada: #" + id);
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
        auditLogService.logCustomAction(AuditAction.INSTALLATION_COMPLETED, "Instalacao", id,
                "Instalação concluída — data de conclusão: " + LocalDate.now());
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
        auditLogService.logCustomAction(AuditAction.INSTALLATION_DELAYED, "Instalacao", id,
                "Instalação adiada para " + novaData + " — motivo: " + motivo.getLabel());
    }
}
