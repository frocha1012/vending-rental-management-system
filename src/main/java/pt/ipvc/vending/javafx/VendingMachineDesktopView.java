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
import pt.ipvc.vending.domain.entity.VendingMachine;
import pt.ipvc.vending.domain.enums.EstadoVendingMachine;
import pt.ipvc.vending.service.VendingMachineService;
import pt.ipvc.vending.service.exception.EntidadeEmUsoException;

import java.math.BigDecimal;

public class VendingMachineDesktopView {

    private final VBox root = new VBox(10);
    private final VendingMachineService vendingMachineService;
    private final TableView<VendingMachine> table = new TableView<>();

    public VendingMachineDesktopView(Stage stage) {
        this.vendingMachineService = DesktopLauncher.getSpringContext().getBean(VendingMachineService.class);
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

        TableColumn<VendingMachine, String> codigoCol = new TableColumn<>("Codigo");
        codigoCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getCodigo()));

        TableColumn<VendingMachine, String> modeloCol = new TableColumn<>("Modelo");
        modeloCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getModelo()));

        TableColumn<VendingMachine, String> localizacaoCol = new TableColumn<>("Localizacao");
        localizacaoCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getLocalizacao() != null ? data.getValue().getLocalizacao() : ""));

        TableColumn<VendingMachine, String> estadoCol = new TableColumn<>("Estado");
        estadoCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getEstado().name()));

        TableColumn<VendingMachine, String> precoCol = new TableColumn<>("Preco Mensal");
        precoCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getPrecoAluguerMensal().toString()));

        table.getColumns().addAll(codigoCol, modeloCol, localizacaoCol, estadoCol, precoCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        Button novoBtn     = new Button("Novo");
        Button editarBtn   = new Button("Editar");
        Button eliminarBtn = new Button("Eliminar");
        Button atualizarBtn = new Button("Atualizar");

        novoBtn.setStyle(theme.primaryButtonStyle());
        editarBtn.setStyle(theme.primaryButtonStyle());
        eliminarBtn.setStyle("-fx-base: #c0392b; -fx-text-fill: white;");

        novoBtn.setOnAction(e -> showForm(null));

        editarBtn.setOnAction(e -> {
            VendingMachine selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showForm(selected);
            }
        });

        eliminarBtn.setOnAction(e -> {
            VendingMachine selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) return;
            try {
                vendingMachineService.eliminar(selected.getId());
                refreshTable();
            } catch (EntidadeEmUsoException ex) {
                showError(ex.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
                showError("Unexpected error: " + ex.getMessage());
            }
        });

        atualizarBtn.setOnAction(e -> refreshTable());

        Label heading = new Label("Vending Machines");
        heading.setStyle(theme.headingStyle());

        HBox actions = new HBox(10, novoBtn, editarBtn, eliminarBtn, atualizarBtn);
        root.getChildren().addAll(heading, table, actions);
    }

    private void refreshTable() {
        try {
            table.setItems(FXCollections.observableArrayList(vendingMachineService.listarTodas()));
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Error loading vending machines: " + ex.getMessage());
        }
    }

    private void showForm(VendingMachine existing) {
        VendingMachine vm = existing != null ? existing : new VendingMachine();

        TextField codigoField = new TextField(vm.getCodigo() != null ? vm.getCodigo() : "");
        TextField modeloField = new TextField(vm.getModelo() != null ? vm.getModelo() : "");
        TextField localizacaoField = new TextField(vm.getLocalizacao() != null ? vm.getLocalizacao() : "");
        ComboBox<EstadoVendingMachine> estadoBox = new ComboBox<>(
                FXCollections.observableArrayList(EstadoVendingMachine.values()));
        estadoBox.setValue(vm.getEstado() != null ? vm.getEstado() : EstadoVendingMachine.DISPONIVEL);
        TextField precoField = new TextField(
                vm.getPrecoAluguerMensal() != null ? vm.getPrecoAluguerMensal().toString() : "0.00");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Codigo:"), codigoField);
        grid.addRow(1, new Label("Modelo:"), modeloField);
        grid.addRow(2, new Label("Localizacao:"), localizacaoField);
        grid.addRow(3, new Label("Estado:"), estadoBox);
        grid.addRow(4, new Label("Preco Mensal:"), precoField);

        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle(existing == null ? "Nova Vending Machine" : "Editar Vending Machine");
        dialog.getDialogPane().setContent(grid);
        dialog.setHeaderText(null);

        dialog.showAndWait().ifPresent(response -> {
            if (response.getButtonData().isDefaultButton()) {
                try {
                    vm.setCodigo(codigoField.getText().trim());
                    vm.setModelo(modeloField.getText().trim());
                    vm.setLocalizacao(localizacaoField.getText().trim());
                    vm.setEstado(estadoBox.getValue());
                    vm.setPrecoAluguerMensal(new BigDecimal(precoField.getText().trim()));
                    vendingMachineService.guardar(vm);
                    refreshTable();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showError("Error saving vending machine: " + ex.getMessage());
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
