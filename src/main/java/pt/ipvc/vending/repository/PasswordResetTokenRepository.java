package pt.ipvc.vending.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pt.ipvc.vending.domain.entity.PasswordResetToken;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    /** Invalidates all unused, non-expired tokens for a given client before issuing a new one. */
    @Modifying
    @Query("UPDATE PasswordResetToken t SET t.used = true " +
           "WHERE t.cliente.id = :clienteId AND t.used = false")
    void invalidatePreviousTokens(@Param("clienteId") Long clienteId);
}
