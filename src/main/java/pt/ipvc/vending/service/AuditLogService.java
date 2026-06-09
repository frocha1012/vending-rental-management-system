package pt.ipvc.vending.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.ipvc.vending.domain.entity.AuditLog;
import pt.ipvc.vending.domain.enums.AuditAction;
import pt.ipvc.vending.repository.AuditLogRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AuditLogService {

    private final AuditLogRepository repo;

    public AuditLogService(AuditLogRepository repo) {
        this.repo = repo;
    }

    // ── Core log method ──────────────────────────────────────────────────────

    public void log(AuditAction action,
                    String entityName, Long entityId,
                    String description,
                    String oldValue, String newValue) {
        AuditLog entry = new AuditLog();
        entry.setTimestamp(LocalDateTime.now());
        entry.setActorRole(AuditContext.getActorRole());
        entry.setActorName(AuditContext.getActorName());
        entry.setAction(action);
        entry.setEntityName(entityName);
        entry.setEntityId(entityId);
        entry.setDescription(description);
        entry.setOldValue(oldValue);
        entry.setNewValue(newValue);
        repo.save(entry);
    }

    public void log(AuditAction action, String entityName, Long entityId, String description) {
        log(action, entityName, entityId, description, null, null);
    }

    // ── Convenience methods ──────────────────────────────────────────────────

    public void logCreate(String entityName, Long entityId, String description) {
        log(AuditAction.CREATE, entityName, entityId, description);
    }

    public void logUpdate(String entityName, Long entityId, String description,
                          String oldValue, String newValue) {
        log(AuditAction.UPDATE, entityName, entityId, description, oldValue, newValue);
    }

    public void logDelete(String entityName, Long entityId, String description) {
        log(AuditAction.DELETE, entityName, entityId, description);
    }

    public void logStatusChange(String entityName, Long entityId, String description,
                                String oldValue, String newValue) {
        log(AuditAction.STATUS_CHANGE, entityName, entityId, description, oldValue, newValue);
    }

    public void logCustomAction(AuditAction action, String entityName, Long entityId,
                                String description) {
        log(action, entityName, entityId, description);
    }

    // ── Queries ──────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<AuditLog> listarTodos() {
        return repo.findAllByOrderByTimestampDesc();
    }
}
