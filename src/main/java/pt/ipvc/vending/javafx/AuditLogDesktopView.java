package pt.ipvc.vending.javafx;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import pt.ipvc.vending.domain.entity.AuditLog;
import pt.ipvc.vending.domain.enums.AuditAction;
import pt.ipvc.vending.service.AuditLogService;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Admin-only JavaFX view for browsing and filtering audit log entries.
 * Logs are loaded from the database and filtered in-memory.
 */
public class AuditLogDesktopView {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final BorderPane root = new BorderPane();
    private final AuditLogService auditLogService;

    private List<AuditLog> allLogs;
    private final ObservableList<AuditLog> displayed = FXCollections.observableArrayList();

    private ComboBox<String> filterAction;
    private ComboBox<String> filterEntity;
    private ComboBox<String> filterRole;

    public AuditLogDesktopView() {
        this.auditLogService = DesktopLauncher.getSpringContext().getBean(AuditLogService.class);
        build();
        refresh();
    }

    public BorderPane getRoot() {
        return root;
    }

    @SuppressWarnings("unchecked")
    private void build() {
        RoleTheme theme = RoleTheme.getCurrent();
        root.setStyle(theme.rootStyle());

        // ── Title ─────────────────────────────────────────────────────────────
        Label title = new Label("Registos de Auditoria");
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // ── Filter bar ────────────────────────────────────────────────────────
        filterAction = new ComboBox<>();
        filterAction.setPromptText("Ação");
        filterAction.setPrefWidth(160);

        filterEntity = new ComboBox<>();
        filterEntity.setPromptText("Entidade");
        filterEntity.setPrefWidth(160);

        filterRole = new ComboBox<>();
        filterRole.setPromptText("Perfil");
        filterRole.setPrefWidth(140);

        Button filterBtn = new Button("Filtrar");
        filterBtn.setStyle(theme.navButtonStyle());
        filterBtn.setOnAction(e -> applyFilters());

        Button clearBtn = new Button("Limpar");
        clearBtn.setStyle("-fx-base: #95a5a6; -fx-text-fill: white;");
        clearBtn.setOnAction(e -> {
            filterAction.getSelectionModel().clearSelection();
            filterEntity.getSelectionModel().clearSelection();
            filterRole.getSelectionModel().clearSelection();
            applyFilters();
        });

        Button refreshBtn = new Button("Atualizar");
        refreshBtn.setStyle(theme.navButtonStyle());
        refreshBtn.setOnAction(e -> refresh());

        HBox filterBar = new HBox(8,
                new Label("Ação:"), filterAction,
                new Label("Entidade:"), filterEntity,
                new Label("Perfil:"), filterRole,
                filterBtn, clearBtn, refreshBtn);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.setPadding(new Insets(6, 0, 6, 0));

        // ── Table ─────────────────────────────────────────────────────────────
        TableView<AuditLog> table = new TableView<>(displayed);
        table.setPlaceholder(new Label("Sem registos de auditoria."));

        TableColumn<AuditLog, String> colTimestamp = new TableColumn<>("Data / Hora");
        colTimestamp.setPrefWidth(145);
        colTimestamp.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getTimestamp().format(FMT)));

        TableColumn<AuditLog, String> colRole = new TableColumn<>("Perfil");
        colRole.setPrefWidth(110);
        colRole.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getActorRole()));

        TableColumn<AuditLog, String> colActor = new TableColumn<>("Utilizador");
        colActor.setPrefWidth(130);
        colActor.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getActorName()));

        TableColumn<AuditLog, String> colAction = new TableColumn<>("Ação");
        colAction.setPrefWidth(150);
        colAction.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getAction().name()));

        TableColumn<AuditLog, String> colEntity = new TableColumn<>("Entidade");
        colEntity.setPrefWidth(120);
        colEntity.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getEntityName() != null
                        ? c.getValue().getEntityName() : "—"));

        TableColumn<AuditLog, String> colId = new TableColumn<>("ID");
        colId.setPrefWidth(55);
        colId.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getEntityId() != null
                        ? String.valueOf(c.getValue().getEntityId()) : "—"));

        TableColumn<AuditLog, String> colDesc = new TableColumn<>("Descrição");
        colDesc.setPrefWidth(320);
        colDesc.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getDescription() != null
                        ? c.getValue().getDescription() : ""));

        table.getColumns().addAll(colTimestamp, colRole, colActor, colAction,
                colEntity, colId, colDesc);

        // ── Layout ────────────────────────────────────────────────────────────
        VBox content = new VBox(8, title, filterBar, table);
        content.setPadding(new Insets(14));
        VBox.setVgrow(table, javafx.scene.layout.Priority.ALWAYS);

        root.setCenter(content);
    }

    private void refresh() {
        try {
            allLogs = auditLogService.listarTodos(); // already sorted newest first
            populateFilterOptions();
            applyFilters();
        } catch (Exception ex) {
            ex.printStackTrace();
            Label err = new Label("Erro ao carregar audit logs: " + ex.getMessage());
            err.setStyle("-fx-text-fill: red; -fx-padding: 10;");
            root.setCenter(err);
        }
    }

    /** Fills the filter ComboBoxes with the distinct values present in the loaded logs. */
    private void populateFilterOptions() {
        String prevAction = filterAction.getValue();
        String prevEntity = filterEntity.getValue();
        String prevRole   = filterRole.getValue();

        List<String> actions = allLogs.stream()
                .map(l -> l.getAction().name())
                .distinct().sorted().collect(Collectors.toList());
        actions.add(0, "TODOS");

        List<String> entities = allLogs.stream()
                .map(AuditLog::getEntityName)
                .filter(e -> e != null && !e.isBlank())
                .distinct().sorted().collect(Collectors.toList());
        entities.add(0, "TODAS");

        List<String> roles = allLogs.stream()
                .map(AuditLog::getActorRole)
                .filter(r -> r != null && !r.isBlank())
                .distinct().sorted().collect(Collectors.toList());
        roles.add(0, "TODOS");

        filterAction.setItems(FXCollections.observableArrayList(actions));
        filterEntity.setItems(FXCollections.observableArrayList(entities));
        filterRole.setItems(FXCollections.observableArrayList(roles));

        // Restore previous selection if still valid
        if (prevAction != null && actions.contains(prevAction)) filterAction.setValue(prevAction);
        if (prevEntity != null && entities.contains(prevEntity)) filterEntity.setValue(prevEntity);
        if (prevRole   != null && roles.contains(prevRole))      filterRole.setValue(prevRole);
    }

    private void applyFilters() {
        if (allLogs == null) return;

        String selAction = filterAction.getValue();
        String selEntity = filterEntity.getValue();
        String selRole   = filterRole.getValue();

        List<AuditLog> result = allLogs.stream()
                .filter(l -> selAction == null || selAction.isBlank()
                        || "TODOS".equals(selAction)
                        || l.getAction().name().equals(selAction))
                .filter(l -> selEntity == null || selEntity.isBlank()
                        || "TODAS".equals(selEntity)
                        || selEntity.equals(l.getEntityName()))
                .filter(l -> selRole == null || selRole.isBlank()
                        || "TODOS".equals(selRole)
                        || selRole.equals(l.getActorRole()))
                .collect(Collectors.toList());

        displayed.setAll(result);
    }
}
