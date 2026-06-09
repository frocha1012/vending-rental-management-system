package pt.ipvc.vending.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.ipvc.vending.domain.entity.Contrato;
import pt.ipvc.vending.repository.ContratoRepository;
import pt.ipvc.vending.repository.InstalacaoRepository;
import pt.ipvc.vending.service.exception.EntidadeEmUsoException;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ContratoService {

    private final ContratoRepository contratoRepository;
    private final InstalacaoRepository instalacaoRepository;
    private final AuditLogService auditLogService;

    public ContratoService(ContratoRepository contratoRepository,
                           InstalacaoRepository instalacaoRepository,
                           AuditLogService auditLogService) {
        this.contratoRepository = contratoRepository;
        this.instalacaoRepository = instalacaoRepository;
        this.auditLogService = auditLogService;
    }

    public List<Contrato> listarTodos() {
        return contratoRepository.findAll();
    }

    public List<Contrato> listarTodosComDetalhes() {
        return contratoRepository.findAllWithDetails();
    }

    public List<Contrato> listarPorCliente(Long clienteId) {
        return contratoRepository.findByClienteIdWithDetails(clienteId);
    }

    public Optional<Contrato> obterPorId(Long id) {
        return contratoRepository.findById(id);
    }

    public Contrato guardar(Contrato contrato) {
        boolean isNew = contrato.getId() == null;
        Contrato saved = contratoRepository.save(contrato);
        if (isNew) {
            auditLogService.logCreate("Contrato", saved.getId(),
                    "Contrato criado — estado: " + saved.getEstado());
        } else {
            auditLogService.logUpdate("Contrato", saved.getId(),
                    "Contrato atualizado — estado: " + saved.getEstado(), null, null);
        }
        return saved;
    }

    public void eliminar(Long id) {
        if (instalacaoRepository.existsByContratoId(id)) {
            throw new EntidadeEmUsoException(
                    "Cannot delete contract because installation records exist.");
        }
        contratoRepository.deleteById(id);
        auditLogService.logDelete("Contrato", id, "Contrato eliminado: #" + id);
    }
}
