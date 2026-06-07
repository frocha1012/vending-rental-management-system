package pt.ipvc.vending.javafx;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import pt.ipvc.vending.domain.entity.PedidoRescisaoContrato;
import pt.ipvc.vending.domain.enums.EstadoPedidoRescisao;
import pt.ipvc.vending.service.PedidoRescisaoContratoService;

public class PedidoRescisaoDesktopView {

    private final VBox root = new VBox(10);
    private final PedidoRescisaoContratoService rescisaoService;
    private final TableView<PedidoRescisaoContrato> table = new TableView<>();

    private final Button aprovarBtn  = new Button("Aprovar");
    private final Button rejeitarBtn = new Button("Rejeitar");
    private final Label  statusLabel = new Label("Selecione um pedido para ver as ações disponíveis.");

    public PedidoRescisaoDesktopView(Stage stage) {
        this.rescisaoService = DesktopLauncher.getSpringContext().getBean(PedidoRescisaoContratoService.class);
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

        // ── Table columns ──────────────────────────────────────────────────────

        TableColumn<PedidoRescisaoContrato, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(d ->
                new SimpleStringProperty(String.valueOf(d.getValue().getId())));

        TableColumn<PedidoRescisaoContrato, String> contratoCol = new TableColumn<>("Contrato");
        contratoCol.setCellValueFactory(d ->
                new SimpleStringProperty("#" + d.getValue().getContrato().getId()));

        TableColumn<PedidoRescisaoContrato, String> clienteCol = new TableColumn<>("Cliente");
        clienteCol.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getContrato().getCliente().getNome()));

        TableColumn<PedidoRescisaoContrato, String> vmCol = new TableColumn<>("Vending Machine");
        vmCol.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().getContrato().getVendingMachine().getCodigo()
                        + " — " + d.getValue().getContrato().getVendingMachine().getModelo()));

        TableColumn<PedidoRescisaoContrato, String> motivoCol = new TableColumn<>("Motivo");
        motivoCol.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getMotivo().name().replace('_', ' ')));

        TableColumn<PedidoRescisaoContrato, String> descricaoCol = new TableColumn<>("Descrição");
        descricaoCol.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().getDescricao() != null ? d.getValue().getDescricao() : "—"));

        TableColumn<PedidoRescisaoContrato, String> dataCol = new TableColumn<>("Data Pedido");
        dataCol.setCellValueFactory(d ->
                new SimpleStringProperty(String.valueOf(d.getValue().getDataPedido())));

        TableColumn<PedidoRescisaoContrato, String> estadoCol = new TableColumn<>("Estado");
        estadoCol.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getEstado().name()));

        table.getColumns().addAll(idCol, contratoCol, clienteCol, vmCol, motivoCol, descricaoCol, dataCol, estadoCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        // ── Initial button states ──────────────────────────────────────────────

        aprovarBtn.setDisable(true);
        rejeitarBtn.setDisable(true);
        aprovarBtn.setStyle("-fx-base: #5cb85c;");
        rejeitarBtn.setStyle("-fx-base: #d9534f;");
        statusLabel.setStyle("-fx-text-fill: grey; -fx-font-size: 11px;");

        // ── Selection listener ─────────────────────────────────────────────────

        table.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, selected) -> updateButtonStates(selected));

        // ── Button actions ─────────────────────────────────────────────────────

        aprovarBtn.setOnAction(e -> {
            PedidoRescisaoContrato selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) return;
            try {
                rescisaoService.aprovar(selected.getId());
                refreshTable();
                showInfo("Pedido #" + selected.getId() + " aprovado.\n"
                        + "Contrato terminado e vending machine disponível.");
            } catch (Exception ex) {
                ex.printStackTrace();
                showError(ex.getMessage());
            }
        });

        rejeitarBtn.setOnAction(e -> {
            PedidoRescisaoContrato selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) return;
            try {
                rescisaoService.rejeitar(selected.getId());
                refreshTable();
                showInfo("Pedido #" + selected.getId() + " rejeitado. Contrato permanece ativo.");
            } catch (Exception ex) {
                ex.printStackTrace();
                showError(ex.getMessage());
            }
        });

        Button atualizarBtn = new Button("Atualizar");
        atualizarBtn.setOnAction(e -> refreshTable());

        HBox actions = new HBox(10, aprovarBtn, rejeitarBtn, atualizarBtn);

        Label heading = new Label("Pedidos de Rescisão de Contrato — Gestão");
        heading.setStyle(theme.headingStyle());

        root.getChildren().addAll(heading, statusLabel, table, actions);
    }

    private void updateButtonStates(PedidoRescisaoContrato selected) {
        if (selected == null) {
            aprovarBtn.setDisable(true);
            rejeitarBtn.setDisable(true);
            statusLabel.setText("Selecione um pedido para ver as ações disponíveis.");
            return;
        }
        boolean isPendente = selected.getEstado() == EstadoPedidoRescisao.PENDENTE;
        aprovarBtn.setDisable(!isPendente);
        rejeitarBtn.setDisable(!isPendente);

        String hint = switch (selected.getEstado()) {
            case PENDENTE  -> "Pedido pendente — pode Aprovar ou Rejeitar.";
            case APROVADO  -> "Pedido aprovado. Contrato terminado.";
            case REJEITADO -> "Pedido rejeitado. Contrato manteve-se ativo.";
        };
        statusLabel.setText("Estado: " + selected.getEstado().name() + "  —  " + hint);
    }

    private void refreshTable() {
        try {
            PedidoRescisaoContrato prev = table.getSelectionModel().getSelectedItem();
            table.setItems(FXCollections.observableArrayList(
                    rescisaoService.listarTodosComDetalhes()));
            if (prev != null) {
                table.getItems().stream()
                        .filter(p -> p.getId().equals(prev.getId()))
                        .findFirst()
                        .ifPresent(p -> table.getSelectionModel().select(p));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Erro ao carregar pedidos: " + ex.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("OK");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
