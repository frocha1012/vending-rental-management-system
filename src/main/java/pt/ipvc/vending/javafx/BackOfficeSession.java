package pt.ipvc.vending.javafx;

import pt.ipvc.vending.domain.enums.BackOfficeRole;

/**
 * Simple in-memory session for the JavaFX BackOffice process.
 * Stores only the minimum needed: id, username and role.
 * Passwords are never stored here.
 */
public final class BackOfficeSession {

    private static Long     userId;
    private static String   username;
    private static BackOfficeRole role;

    private BackOfficeSession() {}

    public static void login(Long id, String uname, BackOfficeRole r) {
        userId   = id;
        username = uname;
        role     = r;
    }

    public static void logout() {
        userId   = null;
        username = null;
        role     = null;
    }

    public static boolean isLoggedIn() {
        return userId != null;
    }

    public static Long getUserId()         { return userId; }
    public static String getUsername()     { return username; }
    public static BackOfficeRole getRole() { return role; }
}
