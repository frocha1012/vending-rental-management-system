package pt.ipvc.vending.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.ipvc.vending.domain.entity.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    java.util.Optional<Cliente> findByUsernameAndPassword(String username, String password);
}
