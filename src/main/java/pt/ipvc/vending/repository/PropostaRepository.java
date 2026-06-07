package pt.ipvc.vending.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pt.ipvc.vending.domain.entity.Proposta;

import java.util.List;

public interface PropostaRepository extends JpaRepository<Proposta, Long> {

    boolean existsByClienteId(Long clienteId);

    boolean existsByVendingMachineId(Long vendingMachineId);

    @Query("SELECT p FROM Proposta p JOIN FETCH p.cliente JOIN FETCH p.vendingMachine WHERE p.cliente.id = :clienteId")
    List<Proposta> findByClienteIdWithDetails(@org.springframework.data.repository.query.Param("clienteId") Long clienteId);

    @Query("SELECT p FROM Proposta p JOIN FETCH p.cliente JOIN FETCH p.vendingMachine")
    List<Proposta> findAllWithDetails();
}
