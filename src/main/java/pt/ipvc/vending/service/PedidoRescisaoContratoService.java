package pt.ipvc.vending.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.ipvc.vending.domain.entity.Contrato;
import pt.ipvc.vending.domain.entity.PedidoRescisaoContrato;
import pt.ipvc.vending.domain.enums.AuditAction;
import pt.ipvc.vending.domain.enums.EstadoContrato;
import pt.ipvc.vending.domain.enums.EstadoPedidoRescisao;
import pt.ipvc.vending.domain.enums.EstadoVendingMachine;
import pt.ipvc.vending.repository.ContratoRepository;
import pt.ipvc.vending.repository.PedidoRescisaoContratoRepository;
import pt.ipvc.vending.repository.VendingMachineRepository;
import pt.ipvc.vending.service.exception.EntidadeEmUsoException;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class PedidoRescisaoContratoService {

    private final PedidoRescisaoContratoRepository pedidoRepository;
    private final ContratoRepository contratoRepository;
    private final VendingMachineRepository vendingMachineRepository;
    private final AuditLogService auditLogService;

    public PedidoRescisaoContratoService(PedidoRescisaoContratoRepository pedidoRepository,
                                          ContratoRepository contratoRepository,
                                          VendingMachineRepository vendingMachineRepository,
                                          AuditLogService auditLogService) {
        this.pedidoRepository = pedidoRepository;
        this.contratoRepository = contratoRepository;
        this.vendingMachineRepository = vendingMachineRepository;
        this.auditLogService = auditLogService;
    }

    public List<PedidoRescisaoContrato> listarTodosComDetalhes() {
        return pedidoRepository.findAllWithDetails();
    }

    public List<PedidoRescisaoContrato> listarPorCliente(Long clienteId) {
        return pedidoRepository.findByClienteIdWithDetails(clienteId);
    }

    public Optional<PedidoRescisaoContrato> obterPorId(Long id) {
        return pedidoRepository.findById(id);
    }

    public PedidoRescisaoContrato guardar(PedidoRescisaoContrato pedido) {
        return pedidoRepository.save(pedido);
    }

    public Set<Long> contratosComPedidoPendente(List<Long> contratoIds) {
        if (contratoIds.isEmpty()) return Set.of();
        return pedidoRepository.findContratoIdsWithEstado(contratoIds, EstadoPedidoRescisao.PENDENTE);
    }

    // Client: submit a rescission request
    public void submeter(PedidoRescisaoContrato pedido) {
        if (pedidoRepository.existsByContratoIdAndEstado(
                pedido.getContrato().getId(), EstadoPedidoRescisao.PENDENTE)) {
            throw new EntidadeEmUsoException(
                    "Já existe um pedido de rescisão pendente para este contrato.");
        }
        PedidoRescisaoContrato saved = pedidoRepository.save(pedido);
        auditLogService.logCustomAction(AuditAction.TERMINATION_REQUEST,
                "PedidoRescisao", saved.getId(),
                "Pedido de rescisão submetido para contrato #"
                + pedido.getContrato().getId()
                + " — motivo: " + pedido.getMotivo().name());
    }

    // Manager: approve → contract TERMINADO, VM back to DISPONIVEL
    public void aprovar(Long id) {
        PedidoRescisaoContrato pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido nao encontrado: " + id));

        pedido.setEstado(EstadoPedidoRescisao.APROVADO);
        pedidoRepository.save(pedido);

        Contrato contrato = pedido.getContrato();
        contrato.setEstado(EstadoContrato.TERMINADO);
        contratoRepository.save(contrato);

        contrato.getVendingMachine().setEstado(EstadoVendingMachine.DISPONIVEL);
        vendingMachineRepository.save(contrato.getVendingMachine());

        auditLogService.logCustomAction(AuditAction.ACCEPT, "PedidoRescisao", id,
                "Rescisão aprovada — contrato #" + contrato.getId()
                + " terminado, máquina " + contrato.getVendingMachine().getCodigo() + " disponível.");
    }

    // Manager: reject → pedido REJEITADO, contract stays ATIVO
    public void rejeitar(Long id) {
        PedidoRescisaoContrato pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido nao encontrado: " + id));
        pedido.setEstado(EstadoPedidoRescisao.REJEITADO);
        pedidoRepository.save(pedido);
        auditLogService.logCustomAction(AuditAction.REJECT, "PedidoRescisao", id,
                "Rescisão rejeitada — contrato #" + pedido.getContrato().getId() + " mantém-se ativo.");
    }
}
