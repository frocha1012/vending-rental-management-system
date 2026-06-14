package pt.ipvc.vending.javafx;

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
import javafx.stage.Stage;
import pt.ipvc.vending.domain.entity.Cliente;
import pt.ipvc.vending.domain.enums.EstadoCliente;
import pt.ipvc.vending.service.ClienteService;
import pt.ipvc.vending.service.exception.EntidadeEmUsoException;

import java.time.LocalDate;

public class ClienteDesktopView {

    private final VBox root = new VBox(10);
    private final ClienteService clienteService;
    private final TableView<Cliente> table = new TableView<>();
    private final BackofficeRole role;

    public ClienteDesktopView(Stage stage, BackofficeRole role) {
        this.clienteService = DesktopLauncher.getSpringContext().getBean(ClienteService.class);
        this.role = role;
        buildLayout();
        refreshTable();
    }

    public VBox getRoot() {
        return root;
    }

    private void buildLayout() {
        RoleTheme theme = RoleTheme.getCurrent();
        root.setPadding(new Insets(10));
        root.setStyle(theme.rootStyle());

        TableColumn<Cliente, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getId()));

        TableColumn<Cliente, String> nomeCol = new TableColumn<>("Nome");
        nomeCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getNome()));

        TableColumn<Cliente, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getEmail()));

        TableColumn<Cliente, String> nifCol = new TableColumn<>("NIF");
        nifCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getNif()));

        TableColumn<Cliente, String> estadoCol = new TableColumn<>("Estado");
        estadoCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getEstado().name()));

        TableColumn<Cliente, String> usernameCol = new TableColumn<>("Username Portal");
        usernameCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getUsername() != null ? data.getValue().getUsername() : "—"));

        table.getColumns().addAll(idCol, nomeCol, emailCol, nifCol, estadoCol, usernameCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        Button novoBtn     = new Button("Novo");
        Button editarBtn   = new Button("Editar");
        Button eliminarBtn = new Button("Eliminar");
        Button atualizarBtn = new Button("Atualizar");

        novoBtn.setStyle(theme.primaryButtonStyle());
        editarBtn.setStyle(theme.primaryButtonStyle());
        eliminarBtn.setStyle("-fx-base: #c0392b; -fx-text-fill: white;");

        boolean canDelete = role != BackofficeRole.RECECIONISTA;
        eliminarBtn.setDisable(!canDelete);
        if (!canDelete) {
            eliminarBtn.setStyle("-fx-opacity: 0.5;");
            javafx.scene.control.Tooltip.install(eliminarBtn,
                    new javafx.scene.control.Tooltip("Rececionista não tem permissão para eliminar clientes."));
        }

        novoBtn.setOnAction(e -> showForm(null));

        editarBtn.setOnAction(e -> {
            Cliente selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showForm(selected);
            }
        });

        eliminarBtn.setOnAction(e -> {
            Cliente selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) return;
            try {
                clienteService.eliminar(selected.getId());
                refreshTable();
            } catch (EntidadeEmUsoException ex) {
                showError(ex.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
                showError("Unexpected error: " + ex.getMessage());
            }
        });

        atualizarBtn.setOnAction(e -> refreshTable());

        Label heading = new Label("Clientes"
                + (role == BackofficeRole.RECECIONISTA ? "  [pode criar e editar · sem permissão de eliminar]" : ""));
        heading.setStyle(theme.headingStyle());

        HBox actions = new HBox(10, novoBtn, editarBtn, eliminarBtn, atualizarBtn);
        root.getChildren().addAll(heading, table, actions);
    }

    private void refreshTable() {
        try {
            table.setItems(FXCollections.observableArrayList(clienteService.listarTodos()));
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Error loading clients: " + ex.getMessage());
        }
    }

    private void showForm(Cliente existing) {
        Cliente cliente = existing != null ? existing : new Cliente();

        TextField nomeField     = new TextField(cliente.getNome()     != null ? cliente.getNome()     : "");
        TextField emailField    = new TextField(cliente.getEmail()    != null ? cliente.getEmail()    : "");
        TextField telefoneField = new TextField(cliente.getTelefone() != null ? cliente.getTelefone() : "");
        TextField nifField      = new TextField(cliente.getNif()      != null ? cliente.getNif()      : "");
        TextField usernameField = new TextField(cliente.getUsername() != null ? cliente.getUsername() : "");
        PasswordField passwordField = new PasswordField();
        usernameField.setPromptText("username para o portal (opcional)");
        passwordField.setPromptText(existing != null ? "Deixar em branco para manter" : "password para o portal (opcional)");

        ComboBox<EstadoCliente> estadoBox = new ComboBox<>(
                FXCollections.observableArrayList(EstadoCliente.values()));
        estadoBox.setValue(cliente.getEstado() != null ? cliente.getEstado() : EstadoCliente.ATIVO);

        TextField dataField = new TextField(
                cliente.getDataRegisto() != null
                        ? cliente.getDataRegisto().toString()
                        : LocalDate.now().toString());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Nome:"),                      nomeField);
        grid.addRow(1, new Label("Email:"),                     emailField);
        grid.addRow(2, new Label("Telefone:"),                  telefoneField);
        grid.addRow(3, new Label("NIF:"),                       nifField);
        grid.addRow(4, new Label("Estado:"),                    estadoBox);
        grid.addRow(5, new Label("Data Registo (yyyy-MM-dd):"), dataField);

        // Separator label for portal credentials section
        Label credLabel = new Label("── Acesso ao Portal ──");
        credLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");
        grid.add(credLabel, 0, 6, 2, 1);
        grid.addRow(7, new Label("Username portal:"), usernameField);
        grid.addRow(8, new Label("Password portal:"), passwordField);

        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle(existing == null ? "Novo Cliente" : "Editar Cliente");
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(460);
        dialog.setHeaderText(role == BackofficeRole.RECECIONISTA
                ? "Preencha os dados do cliente e defina as credenciais do portal."
                : null);

        dialog.showAndWait().ifPresent(response -> {
            if (response.getButtonData().isDefaultButton()) {
                try {
                    cliente.setNome(nomeField.getText().trim());
                    cliente.setEmail(emailField.getText().trim());
                    cliente.setTelefone(telefoneField.getText().trim());
                    cliente.setNif(nifField.getText().trim());
                    cliente.setEstado(estadoBox.getValue());
                    cliente.setDataRegisto(LocalDate.parse(dataField.getText().trim()));

                    String u = usernameField.getText().trim();
                    String p = passwordField.getText().trim();
                    cliente.setUsername(u.isEmpty() ? null : u);
                    cliente.setPassword(p.isEmpty() ? null : p);

                    clienteService.guardar(cliente);
                    refreshTable();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showError("Error saving client: " + ex.getMessage());
                }
            }
        });
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
