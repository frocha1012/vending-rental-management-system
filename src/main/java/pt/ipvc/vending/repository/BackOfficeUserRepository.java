package pt.ipvc.vending.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.ipvc.vending.domain.entity.BackOfficeUser;

import java.util.Optional;

public interface BackOfficeUserRepository extends JpaRepository<BackOfficeUser, Long> {

    Optional<BackOfficeUser> findByUsername(String username);

    boolean existsByUsername(String username);
}
