package pt.ipvc.vending.javafx;

import javafx.beans.property.SimpleStringProperty;
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
import pt.ipvc.vending.domain.entity.Instalacao;
import pt.ipvc.vending.domain.enums.EstadoInstalacao;
import pt.ipvc.vending.domain.enums.MotivoAdiamento;
import pt.ipvc.vending.service.InstalacaoService;

import java.time.LocalDate;

public class TecnicoInstalacaoView {

    private final VBox root = new VBox(10);
    private final InstalacaoService instalacaoService;
    private final TableView<Instalacao> table = new TableView<>();

    private final Button concluirBtn = new Button("✔ Concluir");
    private final Button adiarBtn    = new Button("⏱ Adiar");
    private final Label  statusLabel = new Label("Selecione uma instalação para ver as ações disponíveis.");

    public TecnicoInstalacaoView(Stage stage) {
        this.instalacaoService = DesktopLauncher.getSpringContext().getBean(InstalacaoService.class);
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

        // ── Heading ────────────────────────────────────────────────────────────
        Label heading = new Label("Instalações — Técnico");
        heading.setStyle(theme.headingStyle());

        // ── Table ─────────────────────────────────────────────────────────────
        TableColumn<Instalacao, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(d ->
                new SimpleStringProperty(String.valueOf(d.getValue().getId())));
        idCol.setMaxWidth(50);

        TableColumn<Instalacao, String> contratoCol = new TableColumn<>("Contrato");
        contratoCol.setCellValueFactory(d ->
                new SimpleStringProperty("#" + d.getValue().getContrato().getId()));

        TableColumn<Instalacao, String> clienteCol = new TableColumn<>("Cliente");
        clienteCol.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getContrato().getCliente().getNome()));

        TableColumn<Instalacao, String> localCol = new TableColumn<>("Local");
        localCol.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getLocalInstalacao()));

        TableColumn<Instalacao, String> dataAgendadaCol = new TableColumn<>("Data Agendada");
        dataAgendadaCol.setCellValueFactory(d ->
                new SimpleStringProperty(String.valueOf(d.getValue().getDataInstalacao())));

        TableColumn<Instalacao, String> dataConclusaoCol = new TableColumn<>("Data Conclusão");
        dataConclusaoCol.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().getDataConclusao() != null
                                ? String.valueOf(d.getValue().getDataConclusao()) : "—"));

        TableColumn<Instalacao, String> estadoCol = new TableColumn<>("Estado");
        estadoCol.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getEstado().name()));

        TableColumn<Instalacao, String> motivoCol = new TableColumn<>("Motivo Adiamento");
        motivoCol.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().getMotivoAdiamento() != null
                                ? d.getValue().getMotivoAdiamento().getLabel() : "—"));

        table.getColumns().addAll(
                idCol, contratoCol, clienteCol, localCol,
                dataAgendadaCol, dataConclusaoCol, estadoCol, motivoCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        // ── Button styling ─────────────────────────────────────────────────────
        concluirBtn.setStyle("-fx-base: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
        adiarBtn.setStyle(theme.primaryButtonStyle() + " -fx-font-weight: bold;");
        concluirBtn.setDisable(true);
        adiarBtn.setDisable(true);
        statusLabel.setStyle("-fx-text-fill: grey; -fx-font-size: 11px;");

        // ── Selection listener ─────────────────────────────────────────────────
        table.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, selected) -> updateButtonStates(selected));

        // ── Button actions ─────────────────────────────────────────────────────
        concluirBtn.setOnAction(e -> {
            Instalacao selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) return;

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmar Conclusão");
            confirm.setHeaderText("Concluir instalação #" + selected.getId() + "?");
            confirm.setContentText("A data de conclusão será definida como hoje ("
                    + LocalDate.now() + ").");
            confirm.showAndWait().ifPresent(btn -> {
                if (btn.getButtonData().isDefaultButton()) {
                    try {
                        instalacaoService.concluir(selected.getId());
                        refreshTable();
                        showInfo("Instalação #" + selected.getId()
                                + " marcada como CONCLUIDA.\nData de conclusão: " + LocalDate.now());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        showError(ex.getMessage());
                    }
                }
            });
        });

        adiarBtn.setOnAction(e -> {
            Instalacao selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) return;
            showAdiarDialog(selected);
        });

        Button atualizarBtn = new Button("Atualizar");
        atualizarBtn.setOnAction(e -> refreshTable());

        HBox actions = new HBox(10, concluirBtn, adiarBtn, atualizarBtn);

        root.getChildren().addAll(heading, statusLabel, table, actions);
    }

    // ── Adiar dialog ──────────────────────────────────────────────────────────

    private void showAdiarDialog(Instalacao instalacao) {
        TextField novaDataField = new TextField(
                LocalDate.now().plusDays(7).toString());
        novaDataField.setPromptText("yyyy-MM-dd");

        ComboBox<MotivoAdiamento> motivoBox = new ComboBox<>(
                FXCollections.observableArrayList(MotivoAdiamento.values()));
        motivoBox.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(MotivoAdiamento m) {
                return m != null ? m.getLabel() : "";
            }
            @Override public MotivoAdiamento fromString(String s) { return null; }
        });
        motivoBox.setValue(MotivoAdiamento.CLIENTE_AUSENTE);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Instalação:"),
                new Label("#" + instalacao.getId() + " — " + instalacao.getLocalInstalacao()));
        grid.addRow(1, new Label("Nova data (yyyy-MM-dd):"), novaDataField);
        grid.addRow(2, new Label("Motivo:"), motivoBox);

        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle("Adiar Instalação");
        dialog.setHeaderText("Preencha a nova data e o motivo do adiamento.");
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(440);

        dialog.showAndWait().ifPresent(btn -> {
            if (btn.getButtonData().isDefaultButton()) {
                try {
                    LocalDate novaData = LocalDate.parse(novaDataField.getText().trim());
                    MotivoAdiamento motivo = motivoBox.getValue();
                    if (motivo == null) {
                        showError("Selecione um motivo de adiamento.");
                        return;
                    }
                    instalacaoService.adiar(instalacao.getId(), novaData, motivo);
                    refreshTable();
                    showInfo("Instalação #" + instalacao.getId() + " adiada para "
                            + novaData + ".\nMotivo: " + motivo.getLabel());
                } catch (IllegalArgumentException | IllegalStateException ex) {
                    showError(ex.getMessage());
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showError("Formato de data inválido. Use yyyy-MM-dd.");
                }
            }
        });
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void updateButtonStates(Instalacao selected) {
        if (selected == null) {
            concluirBtn.setDisable(true);
            adiarBtn.setDisable(true);
            statusLabel.setText("Selecione uma instalação para ver as ações disponíveis.");
            return;
        }

        boolean isAgendada = selected.getEstado() == EstadoInstalacao.AGENDADA;
        concluirBtn.setDisable(!isAgendada);
        adiarBtn.setDisable(!isAgendada);

        String hint = switch (selected.getEstado()) {
            case AGENDADA  -> "Instalação agendada — pode Concluir ou Adiar.";
            case CONCLUIDA -> "Instalação concluída em " + selected.getDataConclusao() + ".";
            case ADIADA    -> "Adiada para " + selected.getNovaDataAgendada()
                    + ". Motivo: " + (selected.getMotivoAdiamento() != null
                    ? selected.getMotivoAdiamento().getLabel() : "—");
            case EM_CURSO  -> "Instalação em curso.";
            case CANCELADA -> "Instalação cancelada.";
        };
        statusLabel.setText("Estado: " + selected.getEstado().name() + "  —  " + hint);
    }

    private void refreshTable() {
        try {
            Instalacao prev = table.getSelectionModel().getSelectedItem();
            table.setItems(FXCollections.observableArrayList(
                    instalacaoService.listarTodasComDetalhes()));
            if (prev != null) {
                table.getItems().stream()
                        .filter(i -> i.getId().equals(prev.getId()))
                        .findFirst()
                        .ifPresent(i -> table.getSelectionModel().select(i));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Erro ao carregar instalações: " + ex.getMessage());
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
