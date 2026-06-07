package pt.ipvc.vending.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pt.ipvc.vending.domain.entity.Contrato;

import java.util.List;

public interface ContratoRepository extends JpaRepository<Contrato, Long> {

    boolean existsByClienteId(Long clienteId);

    boolean existsByVendingMachineId(Long vendingMachineId);

    @Query("SELECT c FROM Contrato c JOIN FETCH c.cliente JOIN FETCH c.vendingMachine")
    List<Contrato> findAllWithDetails();

    @Query("SELECT c FROM Contrato c JOIN FETCH c.cliente JOIN FETCH c.vendingMachine WHERE c.cliente.id = :clienteId")
    List<Contrato> findByClienteIdWithDetails(@org.springframework.data.repository.query.Param("clienteId") Long clienteId);
}
