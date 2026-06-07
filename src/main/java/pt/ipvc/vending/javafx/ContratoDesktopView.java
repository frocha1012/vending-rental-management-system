package pt.ipvc.vending.javafx;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import pt.ipvc.vending.domain.entity.Cliente;
import pt.ipvc.vending.domain.entity.Contrato;
import pt.ipvc.vending.domain.entity.VendingMachine;
import pt.ipvc.vending.domain.enums.EstadoContrato;
import pt.ipvc.vending.service.ClienteService;
import pt.ipvc.vending.service.ContratoService;
import pt.ipvc.vending.service.VendingMachineService;
import pt.ipvc.vending.service.exception.EntidadeEmUsoException;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ContratoDesktopView {

    private final VBox root = new VBox(10);
    private final ContratoService contratoService;
    private final ClienteService clienteService;
    private final VendingMachineService vendingMachineService;
    private final TableView<Contrato> table = new TableView<>();
    private final boolean readOnly;

    /** Full CRUD constructor (Admin / Manager). */
    public ContratoDesktopView(Stage stage) {
        this(stage, false);
    }

    /** Pass readOnly=true for roles that may only view contracts. */
    public ContratoDesktopView(Stage stage, boolean readOnly) {
        this.contratoService = DesktopLauncher.getSpringContext().getBean(ContratoService.class);
        this.clienteService = DesktopLauncher.getSpringContext().getBean(ClienteService.class);
        this.vendingMachineService = DesktopLauncher.getSpringContext().getBean(VendingMachineService.class);
        this.readOnly = readOnly;
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

        // cliente and vendingMachine are lazy, but we load them via JOIN FETCH in refreshTable()
        // so by the time these cell factories run the data is already in memory
        TableColumn<Contrato, String> clienteCol = new TableColumn<>("Cliente");
        clienteCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getCliente().getNome()));

        TableColumn<Contrato, String> vmCol = new TableColumn<>("Vending Machine");
        vmCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getVendingMachine().getCodigo()));

        TableColumn<Contrato, String> inicioCol = new TableColumn<>("Inicio");
        inicioCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getDataInicio().toString()));

        TableColumn<Contrato, String> valorCol = new TableColumn<>("Valor Mensal");
        valorCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getValorMensal().toString()));

        TableColumn<Contrato, String> estadoCol = new TableColumn<>("Estado");
        estadoCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getEstado().name()));

        table.getColumns().addAll(clienteCol, vmCol, inicioCol, valorCol, estadoCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        Button atualizarBtn = new Button("Atualizar");
        atualizarBtn.setOnAction(e -> refreshTable());

        Label headingLabel;
        HBox actions;

        if (readOnly) {
            headingLabel = new Label("Contratos  \uD83D\uDD12 Modo Leitura");
            headingLabel.setStyle(theme.readOnlyHeadingStyle());
            actions = new HBox(10, atualizarBtn);
        } else {
            headingLabel = new Label("Contratos");
            headingLabel.setStyle(theme.headingStyle());

            Button novoBtn    = new Button("Novo");
            Button editarBtn  = new Button("Editar");
            Button eliminarBtn = new Button("Eliminar");

            novoBtn.setStyle(theme.primaryButtonStyle());
            editarBtn.setStyle(theme.primaryButtonStyle());
            eliminarBtn.setStyle("-fx-base: #c0392b; -fx-text-fill: white;");

            novoBtn.setOnAction(e -> showForm(null));
            editarBtn.setOnAction(e -> {
                Contrato selected = table.getSelectionModel().getSelectedItem();
                if (selected != null) showForm(selected);
            });
            eliminarBtn.setOnAction(e -> {
                Contrato selected = table.getSelectionModel().getSelectedItem();
                if (selected == null) return;
                try {
                    contratoService.eliminar(selected.getId());
                    refreshTable();
                } catch (EntidadeEmUsoException ex) {
                    showError(ex.getMessage());
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showError("Unexpected error: " + ex.getMessage());
                }
            });
            actions = new HBox(10, novoBtn, editarBtn, eliminarBtn, atualizarBtn);
        }

        root.getChildren().addAll(headingLabel, table, actions);
    }

    private void refreshTable() {
        try {
            // JOIN FETCH ensures cliente and vendingMachine are loaded before session closes
            table.setItems(FXCollections.observableArrayList(contratoService.listarTodosComDetalhes()));
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Error loading contracts: " + ex.getMessage());
        }
    }

    private void showForm(Contrato existing) {
        Contrato contrato = existing != null ? existing : new Contrato();

        ComboBox<Cliente> clienteBox = new ComboBox<>(
                FXCollections.observableArrayList(clienteService.listarTodos()));
        clienteBox.setConverter(new StringConverter<>() {
            @Override public String toString(Cliente c) { return c != null ? c.getNome() : ""; }
            @Override public Cliente fromString(String s) { return null; }
        });
        clienteBox.setValue(contrato.getCliente());

        ComboBox<VendingMachine> vmBox = new ComboBox<>(
                FXCollections.observableArrayList(vendingMachineService.listarTodas()));
        vmBox.setConverter(new StringConverter<>() {
            @Override public String toString(VendingMachine vm) { return vm != null ? vm.getCodigo() + " - " + vm.getModelo() : ""; }
            @Override public VendingMachine fromString(String s) { return null; }
        });
        vmBox.setValue(contrato.getVendingMachine());

        TextField inicioField = new TextField(
                contrato.getDataInicio() != null ? contrato.getDataInicio().toString() : LocalDate.now().toString());
        TextField fimField = new TextField(
                contrato.getDataFim() != null ? contrato.getDataFim().toString() : "");
        TextField valorField = new TextField(
                contrato.getValorMensal() != null ? contrato.getValorMensal().toString() : "0.00");
        ComboBox<EstadoContrato> estadoBox = new ComboBox<>(FXCollections.observableArrayList(EstadoContrato.values()));
        estadoBox.setValue(contrato.getEstado() != null ? contrato.getEstado() : EstadoContrato.RASCUNHO);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Cliente:"), clienteBox);
        grid.addRow(1, new Label("Vending Machine:"), vmBox);
        grid.addRow(2, new Label("Data Inicio (yyyy-MM-dd):"), inicioField);
        grid.addRow(3, new Label("Data Fim (yyyy-MM-dd, opcional):"), fimField);
        grid.addRow(4, new Label("Valor Mensal:"), valorField);
        grid.addRow(5, new Label("Estado:"), estadoBox);

        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle(existing == null ? "Novo Contrato" : "Editar Contrato");
        dialog.getDialogPane().setContent(grid);
        dialog.setHeaderText(null);

        dialog.showAndWait().ifPresent(response -> {
            if (response.getButtonData().isDefaultButton()) {
                try {
                    contrato.setCliente(clienteBox.getValue());
                    contrato.setVendingMachine(vmBox.getValue());
                    contrato.setDataInicio(LocalDate.parse(inicioField.getText().trim()));
                    String fimText = fimField.getText().trim();
                    contrato.setDataFim(fimText.isEmpty() ? null : LocalDate.parse(fimText));
                    contrato.setValorMensal(new BigDecimal(valorField.getText().trim()));
                    contrato.setEstado(estadoBox.getValue());
                    contratoService.guardar(contrato);
                    refreshTable();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showError("Error saving contract: " + ex.getMessage());
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
