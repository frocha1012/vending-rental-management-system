package pt.ipvc.vending.domain.enums;

public enum AuditAction {
    CREATE,
    UPDATE,
    DELETE,
    STATUS_CHANGE,
    LOGIN,
    LOGIN_FAILED,
    LOGOUT,
    ACCEPT,
    REJECT,
    COUNTER_PROPOSAL,
    TERMINATION_REQUEST,
    INSTALLATION_COMPLETED,
    INSTALLATION_DELAYED
}
