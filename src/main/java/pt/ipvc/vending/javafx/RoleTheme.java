package pt.ipvc.vending.javafx;

/**
 * Holds role-specific palette values and produces inline-CSS style strings
 * used consistently across all BackOffice views.
 *
 * Usage: call RoleTheme.setCurrent(RoleTheme.forRole(role)) once on role
 * selection, then call RoleTheme.getCurrent() inside any view's buildLayout().
 */
public class RoleTheme {

    // ── Static singleton ────────────────────────────────────────────────────────
    private static RoleTheme current;

    public static void setCurrent(RoleTheme theme) { current = theme; }

    public static RoleTheme getCurrent() {
        return current != null ? current : forRole(BackofficeRole.ADMIN);
    }

    // ── Palette ─────────────────────────────────────────────────────────────────

    /** Saturated role colour — used for buttons and accents. */
    public final String primary;
    /** Very light tint — panel / content background. */
    public final String lightBg;
    /** Slightly more saturated — top header bar background. */
    public final String headerBg;
    /** Dark, readable version of the colour — headings and labels. */
    public final String darkText;

    private RoleTheme(String primary, String lightBg, String headerBg, String darkText) {
        this.primary    = primary;
        this.lightBg    = lightBg;
        this.headerBg   = headerBg;
        this.darkText   = darkText;
    }

    // ── Style-string helpers ─────────────────────────────────────────────────────

    /** Background for the root content pane of every view. */
    public String rootStyle() {
        return "-fx-background-color: " + lightBg + ";";
    }

    /** Header bar: light bg + 2-px bottom border in primary colour. */
    public String headerBarStyle() {
        return "-fx-background-color: " + headerBg + ";"
                + "-fx-border-color: " + primary + ";"
                + "-fx-border-width: 0 0 2 0;";
    }

    /** Large navigation buttons along the top menu. */
    public String navButtonStyle() {
        return "-fx-base: " + primary + "; -fx-text-fill: white; -fx-font-weight: bold;";
    }

    /** Primary content action buttons (Novo, Editar, Analisar, Enviar…). */
    public String primaryButtonStyle() {
        return "-fx-base: " + primary + "; -fx-text-fill: white;";
    }

    /** Section heading label inside each content view. */
    public String headingStyle() {
        return "-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + darkText + ";";
    }

    /** "Modo Leitura" heading for read-only views. */
    public String readOnlyHeadingStyle() {
        return "-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + primary + ";";
    }

    /** Role name label shown in the top-right corner of the main window. */
    public String roleLabelStyle() {
        return "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + primary + ";";
    }

    /** "Trocar Perfil" outline button. */
    public String switchButtonStyle() {
        return "-fx-background-color: white;"
                + "-fx-border-color: " + primary + ";"
                + "-fx-border-radius: 3;"
                + "-fx-text-fill: " + darkText + ";"
                + "-fx-font-size: 11px;";
    }

    /** Table background tint (does not affect row selection). */
    public String tableStyle() {
        return "-fx-background-color: white;";
    }

    // ── Factory ──────────────────────────────────────────────────────────────────

    public static RoleTheme forRole(BackofficeRole role) {
        return switch (role) {
            case ADMIN        -> new RoleTheme("#2980b9", "#eaf4fb", "#d6eaf8", "#1a5276");
            case MANAGER      -> new RoleTheme("#27ae60", "#eafaf1", "#d5f5e3", "#196f3d");
            case RECECIONISTA -> new RoleTheme("#8e44ad", "#f5eafb", "#e8daef", "#5b2c6f");
            case TECNICO      -> new RoleTheme("#d35400", "#fdf2e9", "#fde5d0", "#a04000");
        };
    }
}
