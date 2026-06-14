package pt.ipvc.vending.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.ipvc.vending.domain.entity.Cliente;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByNif(String nif);

    boolean existsByEmail(String email);
}
