package pt.ipvc.vending.javafx;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import pt.ipvc.vending.domain.entity.AccountRequest;
import pt.ipvc.vending.domain.enums.EstadoAccountRequest;
import pt.ipvc.vending.service.AccountRequestService;
import pt.ipvc.vending.service.AuditContext;

import java.util.List;

public class AccountRequestView {

    private final VBox root = new VBox(10);
    private final AccountRequestService service;
    private final TableView<AccountRequest> table = new TableView<>();
    private final CheckBox mostrarTodosCheck = new CheckBox("Mostrar todos (incluindo processados)");

    public AccountRequestView() {
        this.service = DesktopLauncher.getSpringContext().getBean(AccountRequestService.class);
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

        Label heading = new Label("Pedidos de Criação de Conta  [Admin / Rececionista]");
        heading.setStyle(theme.headingStyle());

        // ── Table columns ──────────────────────────────────────────────────────
        TableColumn<AccountRequest, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getId()));
        idCol.setPrefWidth(45);

        TableColumn<AccountRequest, String> nomeCol = new TableColumn<>("Nome");
        nomeCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNome()));
        nomeCol.setPrefWidth(160);

        TableColumn<AccountRequest, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUsernameRequested()));
        usernameCol.setPrefWidth(100);

        TableColumn<AccountRequest, String> nifCol = new TableColumn<>("NIF");
        nifCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNif()));
        nifCol.setPrefWidth(90);

        TableColumn<AccountRequest, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmail()));
        emailCol.setPrefWidth(180);

        TableColumn<AccountRequest, String> dataCol = new TableColumn<>("Data");
        dataCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDataPedido().toString()));
        dataCol.setPrefWidth(90);

        TableColumn<AccountRequest, EstadoAccountRequest> estadoCol = new TableColumn<>("Estado");
        estadoCol.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getEstado()));
        estadoCol.setPrefWidth(95);
        estadoCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(EstadoAccountRequest estado, boolean empty) {
                super.updateItem(estado, empty);
                if (empty || estado == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(estado.name());
                    switch (estado) {
                        case PENDENTE  -> setStyle("-fx-background-color: #fff3cd; -fx-text-fill: #856404; -fx-font-weight: bold;");
                        case APROVADO  -> setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724; -fx-font-weight: bold;");
                        case REJEITADO -> setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #721c24; -fx-font-weight: bold;");
                    }
                }
            }
        });

        table.getColumns().addAll(idCol, nomeCol, usernameCol, nifCol, emailCol, dataCol, estadoCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPrefHeight(340);

        // ── Actions ────────────────────────────────────────────────────────────
        Button approveBtn  = new Button("Aprovar");
        Button rejectBtn   = new Button("Rejeitar");
        Button detailsBtn  = new Button("Ver Detalhes");
        Button refreshBtn  = new Button("Atualizar");

        approveBtn.setStyle("-fx-base: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
        rejectBtn.setStyle("-fx-base: #c0392b; -fx-text-fill: white; -fx-font-weight: bold;");
        detailsBtn.setStyle(theme.primaryButtonStyle());

        approveBtn.setOnAction(e -> approveSelected());
        rejectBtn.setOnAction(e -> rejectSelected());
        detailsBtn.setOnAction(e -> showDetails());
        refreshBtn.setOnAction(e -> refresh());
        mostrarTodosCheck.setOnAction(e -> refresh());

        HBox actions = new HBox(10, approveBtn, rejectBtn, detailsBtn, refreshBtn);
        root.getChildren().addAll(heading, mostrarTodosCheck, table, actions);
    }

    // ── Approve ───────────────────────────────────────────────────────────────

    private void approveSelected() {
        AccountRequest selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) { showError("Selecione um pedido."); return; }
        if (selected.getEstado() != EstadoAccountRequest.PENDENTE) {
            showError("Apenas pedidos PENDENTES podem ser aprovados."); return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Aprovar Pedido");
        confirm.setHeaderText("Aprovar pedido de: " + selected.getNome());
        confirm.setContentText("Será criada uma conta de cliente para o username '"
                + selected.getUsernameRequested() + "'. Confirma?");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn.getButtonData().isDefaultButton()) {
                setActor();
                try {
                    service.aprovar(selected.getId());
                    refresh();
                    showInfo("Conta criada com sucesso para '" + selected.getUsernameRequested() + "'.");
                } catch (Exception ex) {
                    showError("Erro ao aprovar: " + ex.getMessage());
                }
            }
        });
    }

    // ── Reject ────────────────────────────────────────────────────────────────

    private void rejectSelected() {
        AccountRequest selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) { showError("Selecione um pedido."); return; }
        if (selected.getEstado() != EstadoAccountRequest.PENDENTE) {
            showError("Apenas pedidos PENDENTES podem ser rejeitados."); return;
        }

        TextArea obsField = new TextArea();
        obsField.setPromptText("Motivo / observações (opcional)");
        obsField.setPrefRowCount(3);
        obsField.setWrapText(true);

        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle("Rejeitar Pedido");
        dialog.setHeaderText("Rejeitar pedido de: " + selected.getNome()
                + "  (username: " + selected.getUsernameRequested() + ")");
        dialog.getDialogPane().setContent(obsField);
        dialog.getDialogPane().setPrefWidth(420);

        dialog.showAndWait().ifPresent(btn -> {
            if (btn.getButtonData().isDefaultButton()) {
                setActor();
                try {
                    service.rejeitar(selected.getId(), obsField.getText());
                    refresh();
                    showInfo("Pedido de '" + selected.getUsernameRequested() + "' rejeitado.");
                } catch (Exception ex) {
                    showError("Erro ao rejeitar: " + ex.getMessage());
                }
            }
        });
    }

    // ── Details ───────────────────────────────────────────────────────────────

    private void showDetails() {
        AccountRequest selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) { showError("Selecione um pedido."); return; }

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(8);

        int row = 0;
        grid.addRow(row++, bold("Nome:"),             new Label(selected.getNome()));
        grid.addRow(row++, bold("NIF:"),              new Label(selected.getNif()));
        grid.addRow(row++, bold("Email:"),            new Label(selected.getEmail()));
        grid.addRow(row++, bold("Telefone:"),         new Label(nvl(selected.getTelefone())));
        grid.addRow(row++, bold("Morada:"),           new Label(nvl(selected.getMorada())));
        grid.addRow(row++, bold("Username pedido:"),  new Label(selected.getUsernameRequested()));
        grid.addRow(row++, bold("Data do pedido:"),   new Label(selected.getDataPedido().toString()));
        grid.addRow(row++, bold("Estado:"),           new Label(selected.getEstado().name()));
        grid.addRow(row,   bold("Observações:"),      new Label(nvl(selected.getObservacoes())));

        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Detalhes do Pedido #" + selected.getId());
        info.setHeaderText(null);
        info.getDialogPane().setContent(grid);
        info.getDialogPane().setPrefWidth(460);
        info.showAndWait();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void refresh() {
        try {
            List<AccountRequest> list = mostrarTodosCheck.isSelected()
                    ? service.listarTodos()
                    : service.listarPendentes();
            table.setItems(FXCollections.observableArrayList(list));
        } catch (Exception ex) {
            showError("Erro ao carregar pedidos: " + ex.getMessage());
        }
    }

    private void setActor() {
        AuditContext.setActor(BackOfficeSession.getRole().name(), BackOfficeSession.getUsername());
    }

    private Label bold(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-weight: bold;");
        return l;
    }

    private String nvl(String value) {
        return value != null && !value.isBlank() ? value : "—";
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
