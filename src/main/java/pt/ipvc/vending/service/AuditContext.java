package pt.ipvc.vending.service;

/**
 * Thread-local holder for the current actor identity.
 *
 * Set this before calling any mutating service method so that AuditLogService
 * can attribute the change to the correct user/role without needing to pass
 * actor information through every service signature.
 *
 * Web controllers set it per-request; the JavaFX desktop sets it once on
 * role selection (JavaFX runs on a single thread).
 */
public final class AuditContext {

    private static final ThreadLocal<String> ROLE = new ThreadLocal<>();
    private static final ThreadLocal<String> NAME = new ThreadLocal<>();

    private AuditContext() {}

    public static void setActor(String role, String name) {
        ROLE.set(role);
        NAME.set(name);
    }

    public static String getActorRole() {
        String r = ROLE.get();
        return r != null ? r : "SYSTEM";
    }

    public static String getActorName() {
        String n = NAME.get();
        return n != null ? n : "System";
    }

    public static void clear() {
        ROLE.remove();
        NAME.remove();
    }
}
