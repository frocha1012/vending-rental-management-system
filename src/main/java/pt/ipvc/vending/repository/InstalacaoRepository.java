package pt.ipvc.vending.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pt.ipvc.vending.domain.entity.Instalacao;

import java.util.List;

public interface InstalacaoRepository extends JpaRepository<Instalacao, Long> {

    boolean existsByContratoId(Long contratoId);

    @Query("SELECT i FROM Instalacao i JOIN FETCH i.contrato c JOIN FETCH c.cliente JOIN FETCH c.vendingMachine")
    List<Instalacao> findAllWithDetails();

    @Query("SELECT i FROM Instalacao i JOIN FETCH i.contrato c JOIN FETCH c.cliente JOIN FETCH c.vendingMachine WHERE c.cliente.id = :clienteId")
    List<Instalacao> findByClienteIdWithDetails(@org.springframework.data.repository.query.Param("clienteId") Long clienteId);
}
