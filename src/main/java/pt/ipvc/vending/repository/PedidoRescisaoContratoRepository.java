package pt.ipvc.vending.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pt.ipvc.vending.domain.entity.PedidoRescisaoContrato;
import pt.ipvc.vending.domain.enums.EstadoPedidoRescisao;

import java.util.List;
import java.util.Set;

public interface PedidoRescisaoContratoRepository extends JpaRepository<PedidoRescisaoContrato, Long> {

    boolean existsByContratoIdAndEstado(Long contratoId, EstadoPedidoRescisao estado);

    // Fetch all pedidos with full contract/client/vm details for JavaFX manager view
    @Query("SELECT p FROM PedidoRescisaoContrato p " +
           "JOIN FETCH p.contrato c " +
           "JOIN FETCH c.cliente " +
           "JOIN FETCH c.vendingMachine")
    List<PedidoRescisaoContrato> findAllWithDetails();

    // Fetch pedidos for a client's contracts (for portal — to know which have pending requests)
    @Query("SELECT p FROM PedidoRescisaoContrato p " +
           "JOIN FETCH p.contrato c " +
           "JOIN FETCH c.cliente " +
           "JOIN FETCH c.vendingMachine " +
           "WHERE c.cliente.id = :clienteId")
    List<PedidoRescisaoContrato> findByClienteIdWithDetails(@Param("clienteId") Long clienteId);

    // Returns the IDs of contracts that have a PENDENTE pedido (for efficient template check)
    @Query("SELECT p.contrato.id FROM PedidoRescisaoContrato p " +
           "WHERE p.estado = :estado AND p.contrato.id IN :contratoIds")
    Set<Long> findContratoIdsWithEstado(
            @Param("contratoIds") List<Long> contratoIds,
            @Param("estado") EstadoPedidoRescisao estado);
}
