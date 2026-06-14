package pt.ipvc.vending.javafx;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import pt.ipvc.vending.domain.entity.BackOfficeUser;
import pt.ipvc.vending.domain.enums.BackOfficeRole;
import pt.ipvc.vending.service.AuditContext;
import pt.ipvc.vending.service.BackOfficeUserService;

import java.util.Arrays;
import java.util.List;

public class UserManagementView {

    private final VBox root = new VBox(10);
    private final BackOfficeUserService userService;
    private final TableView<BackOfficeUser> table = new TableView<>();

    public UserManagementView() {
        this.userService = DesktopLauncher.getSpringContext().getBean(BackOfficeUserService.class);
        buildLayout();
        refresh();
    }

    public VBox getRoot() {
        return root;
    }

    private void buildLayout() {
        RoleTheme theme = RoleTheme.getCurrent();
        root.setPadding(new Insets(10));
        root.setStyle(theme.rootStyle());

        Label heading = new Label("Utilizadores BackOffice  [apenas ADMIN]");
        heading.setStyle(theme.headingStyle());

        TableColumn<BackOfficeUser, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getId()));
        idCol.setPrefWidth(50);

        TableColumn<BackOfficeUser, String> userCol = new TableColumn<>("Username");
        userCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUsername()));

        TableColumn<BackOfficeUser, String> roleCol = new TableColumn<>("Função");
        roleCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRole().getLabel()));

        TableColumn<BackOfficeUser, Boolean> activeCol = new TableColumn<>("Estado");
        activeCol.setCellValueFactory(d -> new SimpleBooleanProperty(d.getValue().isActive()).asObject());
        activeCol.setCellFactory(col -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(Boolean active, boolean empty) {
                super.updateItem(active, empty);
                if (empty || active == null) {
                    setText(null);
                    setStyle("");
                } else if (active) {
                    setText("Ativo");
                    setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724; -fx-font-weight: bold;");
                } else {
                    setText("Inativo");
                    setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #721c24; -fx-font-weight: bold;");
                }
            }
        });

        TableColumn<BackOfficeUser, String> createdCol = new TableColumn<>("Criado em");
        createdCol.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getCreatedAt().toLocalDate().toString()));

        table.getColumns().addAll(idCol, userCol, roleCol, activeCol, createdCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        Button novoBtn       = new Button("Novo Utilizador");
        Button toggleBtn     = new Button("Ativar / Desativar");
        Button resetPwdBtn   = new Button("Repor Password");
        Button refreshBtn    = new Button("Atualizar");

        novoBtn.setStyle(theme.primaryButtonStyle());
        toggleBtn.setStyle(theme.primaryButtonStyle());
        resetPwdBtn.setStyle("-fx-base: #e67e22; -fx-text-fill: white;");

        novoBtn.setOnAction(e -> showCreateForm());
        toggleBtn.setOnAction(e -> toggleSelected());
        resetPwdBtn.setOnAction(e -> resetPasswordForSelected());
        refreshBtn.setOnAction(e -> refresh());

        HBox actions = new HBox(10, novoBtn, toggleBtn, resetPwdBtn, refreshBtn);
        root.getChildren().addAll(heading, table, actions);
    }

    // ── Create user ───────────────────────────────────────────────────────────

    private void showCreateForm() {
        TextField usernameField = new TextField();
        usernameField.setPromptText("username");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("password inicial");

        List<BackOfficeRole> allowedRoles = Arrays.asList(
                BackOfficeRole.GESTOR, BackOfficeRole.RECECIONISTA, BackOfficeRole.TECNICO);
        ComboBox<BackOfficeRole> roleBox = new ComboBox<>(
                FXCollections.observableArrayList(allowedRoles));
        roleBox.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(BackOfficeRole r) { return r != null ? r.getLabel() : ""; }
            @Override public BackOfficeRole fromString(String s) { return null; }
        });
        roleBox.setValue(BackOfficeRole.GESTOR);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Username:"), usernameField);
        grid.addRow(1, new Label("Password:"), passwordField);
        grid.addRow(2, new Label("Função:"),   roleBox);

        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle("Novo Utilizador BackOffice");
        dialog.setHeaderText("Criar novo utilizador (não é possível criar ADMIN)");
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(380);

        dialog.showAndWait().ifPresent(response -> {
            if (response.getButtonData().isDefaultButton()) {
                setActor();
                try {
                    userService.criar(usernameField.getText(), passwordField.getText(), roleBox.getValue());
                    refresh();
                    showInfo("Utilizador '" + usernameField.getText().trim() + "' criado com sucesso.");
                } catch (IllegalArgumentException ex) {
                    showError(ex.getMessage());
                } catch (Exception ex) {
                    showError("Erro ao criar utilizador: " + ex.getMessage());
                }
            }
        });
    }

    // ── Toggle active ─────────────────────────────────────────────────────────

    private void toggleSelected() {
        BackOfficeUser selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) { showError("Selecione um utilizador."); return; }
        setActor();
        try {
            BackOfficeUser updated = userService.toggleActive(selected.getId());
            refresh();
            showInfo("Utilizador '" + updated.getUsername() + "' está agora "
                    + (updated.isActive() ? "ATIVO" : "INATIVO") + ".");
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        } catch (Exception ex) {
            showError("Erro: " + ex.getMessage());
        }
    }

    // ── Reset password ────────────────────────────────────────────────────────

    private void resetPasswordForSelected() {
        BackOfficeUser selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) { showError("Selecione um utilizador."); return; }

        PasswordField newPwd = new PasswordField();
        newPwd.setPromptText("nova password");
        PasswordField confirmPwd = new PasswordField();
        confirmPwd.setPromptText("confirmar password");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Nova password:"),      newPwd);
        grid.addRow(1, new Label("Confirmar password:"), confirmPwd);

        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle("Repor Password");
        dialog.setHeaderText("Repor password de: " + selected.getUsername());
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(360);

        dialog.showAndWait().ifPresent(response -> {
            if (response.getButtonData().isDefaultButton()) {
                if (!newPwd.getText().equals(confirmPwd.getText())) {
                    showError("As passwords não coincidem.");
                    return;
                }
                setActor();
                try {
                    userService.resetPassword(selected.getId(), newPwd.getText());
                    showInfo("Password de '" + selected.getUsername() + "' reposta com sucesso.");
                } catch (IllegalArgumentException ex) {
                    showError(ex.getMessage());
                } catch (Exception ex) {
                    showError("Erro: " + ex.getMessage());
                }
            }
        });
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void refresh() {
        try {
            table.setItems(FXCollections.observableArrayList(userService.listarTodos()));
        } catch (Exception ex) {
            showError("Erro ao carregar utilizadores: " + ex.getMessage());
        }
    }

    private void setActor() {
        AuditContext.setActor(BackOfficeSession.getRole().name(), BackOfficeSession.getUsername());
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Erro");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Sucesso");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
