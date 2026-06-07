package pt.ipvc.vending.javafx;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import pt.ipvc.vending.domain.entity.Contrato;
import pt.ipvc.vending.domain.entity.Instalacao;
import pt.ipvc.vending.domain.enums.EstadoInstalacao;
import pt.ipvc.vending.service.ContratoService;
import pt.ipvc.vending.service.InstalacaoService;

import java.time.LocalDate;

public class InstalacaoDesktopView {

    private final VBox root = new VBox(10);
    private final InstalacaoService instalacaoService;
    private final ContratoService contratoService;
    private final TableView<Instalacao> table = new TableView<>();

    public InstalacaoDesktopView(Stage stage) {
        this.instalacaoService = DesktopLauncher.getSpringContext().getBean(InstalacaoService.class);
        this.contratoService = DesktopLauncher.getSpringContext().getBean(ContratoService.class);
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

        // contrato is LAZY — data is loaded via JOIN FETCH in refreshTable()
        TableColumn<Instalacao, String> contratoCol = new TableColumn<>("Contrato");
        contratoCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        "#" + data.getValue().getContrato().getId()
                        + " — " + data.getValue().getContrato().getCliente().getNome()));

        TableColumn<Instalacao, String> dataCol = new TableColumn<>("Data Instalacao");
        dataCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getDataInstalacao().toString()));

        TableColumn<Instalacao, String> localCol = new TableColumn<>("Local");
        localCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getLocalInstalacao()));

        TableColumn<Instalacao, String> estadoCol = new TableColumn<>("Estado");
        estadoCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getEstado().name()));

        table.getColumns().addAll(contratoCol, dataCol, localCol, estadoCol);
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
            Instalacao selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showForm(selected);
            }
        });

        eliminarBtn.setOnAction(e -> {
            Instalacao selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) return;
            try {
                instalacaoService.eliminar(selected.getId());
                refreshTable();
            } catch (Exception ex) {
                ex.printStackTrace();
                showError("Error deleting installation: " + ex.getMessage());
            }
        });

        atualizarBtn.setOnAction(e -> refreshTable());

        Label heading = new Label("Instalações");
        heading.setStyle(theme.headingStyle());

        HBox actions = new HBox(10, novoBtn, editarBtn, eliminarBtn, atualizarBtn);
        root.getChildren().addAll(heading, table, actions);
    }

    private void refreshTable() {
        try {
            table.setItems(FXCollections.observableArrayList(
                    instalacaoService.listarTodasComDetalhes()));
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Error loading installations: " + ex.getMessage());
        }
    }

    private void showForm(Instalacao existing) {
        Instalacao instalacao = existing != null ? existing : new Instalacao();

        ComboBox<Contrato> contratoBox = new ComboBox<>(
                FXCollections.observableArrayList(contratoService.listarTodosComDetalhes()));
        contratoBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Contrato c) {
                if (c == null) return "";
                return "#" + c.getId() + " — " + c.getCliente().getNome()
                        + " / " + c.getVendingMachine().getCodigo();
            }
            @Override
            public Contrato fromString(String s) { return null; }
        });
        contratoBox.setValue(instalacao.getContrato());

        TextField dataField = new TextField(
                instalacao.getDataInstalacao() != null
                        ? instalacao.getDataInstalacao().toString()
                        : LocalDate.now().toString());

        TextField localField = new TextField(
                instalacao.getLocalInstalacao() != null ? instalacao.getLocalInstalacao() : "");

        ComboBox<EstadoInstalacao> estadoBox = new ComboBox<>(
                FXCollections.observableArrayList(EstadoInstalacao.values()));
        estadoBox.setValue(instalacao.getEstado() != null
                ? instalacao.getEstado() : EstadoInstalacao.AGENDADA);

        TextArea obsArea = new TextArea(
                instalacao.getObservacoes() != null ? instalacao.getObservacoes() : "");
        obsArea.setPrefRowCount(3);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Contrato:"), contratoBox);
        grid.addRow(1, new Label("Data Instalacao (yyyy-MM-dd):"), dataField);
        grid.addRow(2, new Label("Local Instalacao:"), localField);
        grid.addRow(3, new Label("Estado:"), estadoBox);
        grid.addRow(4, new Label("Observacoes:"), obsArea);

        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle(existing == null ? "Nova Instalacao" : "Editar Instalacao");
        dialog.getDialogPane().setContent(grid);
        dialog.setHeaderText(null);

        dialog.showAndWait().ifPresent(response -> {
            if (response.getButtonData().isDefaultButton()) {
                try {
                    instalacao.setContrato(contratoBox.getValue());
                    instalacao.setDataInstalacao(LocalDate.parse(dataField.getText().trim()));
                    instalacao.setLocalInstalacao(localField.getText().trim());
                    instalacao.setEstado(estadoBox.getValue());
                    instalacao.setObservacoes(obsArea.getText().trim());
                    instalacaoService.guardar(instalacao);
                    refreshTable();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showError("Error saving installation: " + ex.getMessage());
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
