package pt.ipvc.vending.domain.enums;

public enum BackOfficeRole {
    ADMIN("Administrador"),
    GESTOR("Gestor"),
    RECECIONISTA("Rececionista"),
    TECNICO("Técnico");

    private final String label;

    BackOfficeRole(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
