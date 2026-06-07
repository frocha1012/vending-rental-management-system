package pt.ipvc.vending.javafx;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import pt.ipvc.vending.domain.entity.Proposta;
import pt.ipvc.vending.domain.enums.EstadoProposta;
import pt.ipvc.vending.service.PropostaService;

import java.math.BigDecimal;

public class PropostaDesktopView {

    private final VBox root = new VBox(10);
    private final PropostaService propostaService;
    private final TableView<Proposta> table = new TableView<>();
    private final boolean readOnly;

    // Action buttons — only shown in full (non-read-only) mode
    private final Button analisarBtn = new Button("Analisar / Definir Preço");
    private final Button enviarBtn   = new Button("Enviar ao Cliente");
    private final Label  statusLabel = new Label("");

    /** Full manager constructor. */
    public PropostaDesktopView(Stage stage) {
        this(stage, false);
    }

    /** Pass readOnly=true for roles that may only view proposals. */
    public PropostaDesktopView(Stage stage, boolean readOnly) {
        this.propostaService = DesktopLauncher.getSpringContext().getBean(PropostaService.class);
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

        // ── Table columns ──────────────────────────────────────────────────────

        TableColumn<Proposta, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(String.valueOf(d.getValue().getId())));

        TableColumn<Proposta, String> clienteCol = new TableColumn<>("Cliente");
        clienteCol.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().getCliente().getNome()));

        TableColumn<Proposta, String> vmCol = new TableColumn<>("Vending Machine");
        vmCol.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().getVendingMachine().getCodigo()));

        TableColumn<Proposta, String> valorClienteCol = new TableColumn<>("Valor Cliente");
        valorClienteCol.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(
                        d.getValue().getValorProposto() != null
                                ? d.getValue().getValorProposto().toString() + " €"
                                : "—"));

        TableColumn<Proposta, String> valorGestorCol = new TableColumn<>("Valor Gestor");
        valorGestorCol.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(
                        d.getValue().getValorGestor() != null
                                ? d.getValue().getValorGestor().toString() + " €"
                                : "—"));

        TableColumn<Proposta, String> duracaoCol = new TableColumn<>("Duração");
        duracaoCol.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(
                        d.getValue().getDuracaoAnos() != null
                                ? d.getValue().getDuracaoAnos() + " ano(s)"
                                : "—"));

        TableColumn<Proposta, String> estadoCol = new TableColumn<>("Estado");
        estadoCol.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().getEstado().name()));

        table.getColumns().addAll(idCol, clienteCol, vmCol, valorClienteCol, valorGestorCol, duracaoCol, estadoCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        Button atualizarBtn = new Button("Atualizar");
        atualizarBtn.setOnAction(e -> refreshTable());

        Label headingLabel;
        HBox actions;

        if (readOnly) {
            headingLabel = new Label("Propostas  \uD83D\uDD12 Modo Leitura");
            headingLabel.setStyle(theme.readOnlyHeadingStyle());
            actions = new HBox(10, atualizarBtn);
            // No selection listener or action buttons needed in read-only mode
        } else {
            headingLabel = new Label("Propostas — Gestão");
            headingLabel.setStyle(theme.headingStyle());

            // ── Initial button states (disabled until a row is selected) ──────
            analisarBtn.setDisable(true);
            enviarBtn.setDisable(true);
            statusLabel.setStyle("-fx-text-fill: grey; -fx-font-size: 11px;");
            statusLabel.setText("Selecione uma proposta para ver as ações disponíveis.");

            // ── Selection listener ─────────────────────────────────────────────
            table.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldSel, selected) -> updateButtonStates(selected));

            // ── Button actions ─────────────────────────────────────────────────
            analisarBtn.setOnAction(e -> {
                Proposta selected = table.getSelectionModel().getSelectedItem();
                if (selected != null) showAnaliseForm(selected);
            });
            enviarBtn.setOnAction(e -> {
                Proposta selected = table.getSelectionModel().getSelectedItem();
                if (selected == null) return;
                try {
                    propostaService.enviarAoCliente(selected.getId());
                    refreshTable();
                    showInfo("Proposta #" + selected.getId() + " enviada ao cliente com sucesso.");
                } catch (IllegalStateException ex) {
                    showError(ex.getMessage());
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showError("Error: " + ex.getMessage());
                }
            });

            analisarBtn.setStyle(theme.primaryButtonStyle());
            enviarBtn.setStyle(theme.primaryButtonStyle());
            actions = new HBox(10, analisarBtn, enviarBtn, atualizarBtn);
        }

        if (readOnly) {
            root.getChildren().addAll(headingLabel, table, actions);
        } else {
            root.getChildren().addAll(headingLabel, statusLabel, table, actions);
        }
    }

    /**
     * Enables/disables buttons and updates the status label based on the
     * selected proposal's current state.
     *
     * Rules:
     *  - "Analisar" is available for PENDENTE, EM_ANALISE, CONTRAPROPOSTA
     *    (terminal states ACEITE/REJEITADA/EXPIRADA cannot be re-analysed)
     *  - "Enviar ao Cliente" is available only when estado is EM_ANALISE
     *    AND valorGestor is already set
     */
    private void updateButtonStates(Proposta selected) {
        if (selected == null) {
            analisarBtn.setDisable(true);
            enviarBtn.setDisable(true);
            statusLabel.setText("Selecione uma proposta para ver as ações disponíveis.");
            return;
        }

        EstadoProposta estado = selected.getEstado();

        boolean podeAnalisar = estado == EstadoProposta.PENDENTE
                || estado == EstadoProposta.EM_ANALISE
                || estado == EstadoProposta.CONTRAPROPOSTA;

        boolean podeEnviar = estado == EstadoProposta.EM_ANALISE
                && selected.getValorGestor() != null;

        analisarBtn.setDisable(!podeAnalisar);
        enviarBtn.setDisable(!podeEnviar);

        // Human-readable guidance in the status bar
        String hint;
        switch (estado) {
            case PENDENTE       -> hint = "Nova proposta do cliente. Clique em 'Analisar' para definir o preço.";
            case CONTRAPROPOSTA -> hint = "O cliente fez uma contraproposta. Reveja e defina novo preço.";
            case EM_ANALISE     -> hint = selected.getValorGestor() != null
                    ? "Preço definido (" + selected.getValorGestor() + " €). Pode enviar ao cliente."
                    : "Em análise — defina o preço gestor antes de enviar ao cliente.";
            case ENVIADA_CLIENTE -> hint = "A aguardar resposta do cliente.";
            case ACEITE          -> hint = "Proposta aceite. Contrato e instalação foram criados.";
            case REJEITADA       -> hint = "Proposta rejeitada pelo cliente.";
            case EXPIRADA        -> hint = "Proposta expirada.";
            default              -> hint = estado.name();
        }
        statusLabel.setText("Estado: " + estado.name() + "  —  " + hint);
    }

    private void refreshTable() {
        try {
            Proposta previousSelection = table.getSelectionModel().getSelectedItem();
            table.setItems(FXCollections.observableArrayList(
                    propostaService.listarTodasComDetalhes()));
            // Restore selection if the same row is still in the list
            if (previousSelection != null) {
                table.getItems().stream()
                        .filter(p -> p.getId().equals(previousSelection.getId()))
                        .findFirst()
                        .ifPresent(p -> table.getSelectionModel().select(p));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Error loading proposals: " + ex.getMessage());
        }
    }

    private void showAnaliseForm(Proposta proposta) {
        // Pre-fill with manager's existing price or fall back to client's price
        TextField valorField = new TextField(
                proposta.getValorGestor() != null
                        ? proposta.getValorGestor().toString()
                        : proposta.getValorProposto() != null
                                ? proposta.getValorProposto().toString()
                                : "");

        TextArea obsArea = new TextArea(
                proposta.getObservacoesGestor() != null ? proposta.getObservacoesGestor() : "");
        obsArea.setPrefRowCount(3);
        obsArea.setPromptText("Observações para o cliente (opcional)");

        String duracaoTexto = proposta.getDuracaoAnos() != null
                ? proposta.getDuracaoAnos() + " ano(s)" : "não especificada";

        Label clienteInfo = new Label(
                "Cliente: " + proposta.getCliente().getNome()
                + "     VM: " + proposta.getVendingMachine().getCodigo()
                + " — " + proposta.getVendingMachine().getModelo()
                + "     Valor cliente: " + proposta.getValorProposto() + " €"
                + "     Duração: " + duracaoTexto);
        clienteInfo.setStyle("-fx-font-weight: bold;");
        clienteInfo.setWrapText(true);

        Label obsCliente = new Label(
                proposta.getObservacoes() != null && !proposta.getObservacoes().isBlank()
                        ? "Nota do cliente: " + proposta.getObservacoes()
                        : "");
        obsCliente.setStyle("-fx-text-fill: #555;");
        obsCliente.setWrapText(true);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.addRow(0, clienteInfo);
        grid.addRow(1, obsCliente);
        grid.addRow(2, new Label("Preço gestor (€) *:"), valorField);
        grid.addRow(3, new Label("Observações gestor:"), obsArea);

        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle("Analisar Proposta #" + proposta.getId());
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(520);
        dialog.setHeaderText(
                "Defina o preço do gestor (*obrigatório) e as observações.\n"
                + "O estado passará a EM_ANALISE. Depois clique 'Enviar ao Cliente'.");

        dialog.showAndWait().ifPresent(response -> {
            if (response.getButtonData().isDefaultButton()) {
                try {
                    String valorText = valorField.getText().trim();
                    if (valorText.isEmpty()) {
                        showError("O preço do gestor é obrigatório.");
                        return;
                    }
                    BigDecimal valor = new BigDecimal(valorText);
                    propostaService.tomarEmAnalise(proposta.getId(), valor, obsArea.getText().trim());
                    refreshTable();
                } catch (NumberFormatException ex) {
                    showError("Formato de preço inválido. Use ex: 150.00");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showError("Error saving: " + ex.getMessage());
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

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("OK");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
