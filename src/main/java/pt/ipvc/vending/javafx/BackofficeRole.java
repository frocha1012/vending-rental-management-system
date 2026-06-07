package pt.ipvc.vending.javafx;

public enum BackofficeRole {
    ADMIN("Administrador"),
    MANAGER("Gestor"),
    RECECIONISTA("Rececionista"),
    TECNICO("Técnico");

    private final String label;

    BackofficeRole(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
