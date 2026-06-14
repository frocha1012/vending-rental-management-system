package pt.ipvc.vending.javafx;

/**
 * JavaFX-layer alias kept so existing view constructors compile without change.
 * Maps 1-to-1 to the domain enum {@link pt.ipvc.vending.domain.enums.BackOfficeRole}.
 * NOTE: MANAGER was renamed to GESTOR to match the domain model.
 */
public enum BackofficeRole {
    ADMIN("Administrador"),
    GESTOR("Gestor"),
    RECECIONISTA("Rececionista"),
    TECNICO("Técnico");

    private final String label;

    BackofficeRole(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /** Converts to the domain enum used by entities and services. */
    public pt.ipvc.vending.domain.enums.BackOfficeRole toDomain() {
        return pt.ipvc.vending.domain.enums.BackOfficeRole.valueOf(this.name());
    }

    /** Converts from the domain enum. */
    public static BackofficeRole fromDomain(pt.ipvc.vending.domain.enums.BackOfficeRole r) {
        return BackofficeRole.valueOf(r.name());
    }
}
