package pt.ipvc.vending.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.ipvc.vending.domain.entity.Contrato;
import pt.ipvc.vending.domain.entity.PedidoRescisaoContrato;
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

    public PedidoRescisaoContratoService(PedidoRescisaoContratoRepository pedidoRepository,
                                          ContratoRepository contratoRepository,
                                          VendingMachineRepository vendingMachineRepository) {
        this.pedidoRepository = pedidoRepository;
        this.contratoRepository = contratoRepository;
        this.vendingMachineRepository = vendingMachineRepository;
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

    // Returns IDs of contracts (from the given list) that already have a PENDENTE pedido
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
        pedidoRepository.save(pedido);
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
    }

    // Manager: reject → pedido REJEITADO, contract stays ATIVO
    public void rejeitar(Long id) {
        PedidoRescisaoContrato pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido nao encontrado: " + id));
        pedido.setEstado(EstadoPedidoRescisao.REJEITADO);
        pedidoRepository.save(pedido);
    }
}
