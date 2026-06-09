package pt.ipvc.vending.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.ipvc.vending.domain.entity.AuditLog;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findAllByOrderByTimestampDesc();
}
