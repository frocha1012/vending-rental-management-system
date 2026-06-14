package pt.ipvc.vending.javafx;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class DesktopMainView {

    private final BorderPane root = new BorderPane();
    private final Stage stage;
    private final BackofficeRole role;
    private final Runnable onLogout;

    public DesktopMainView(Stage stage, BackofficeRole role, Runnable onLogout) {
        this.stage    = stage;
        this.role     = role;
        this.onLogout = onLogout;
        buildLayout();
        showHome();
    }

    public BorderPane getRoot() {
        return root;
    }

    private void buildLayout() {
        RoleTheme theme = RoleTheme.getCurrent();

        root.setStyle(theme.rootStyle());

        // ── Header bar ────────────────────────────────────────────────────────
        Label title = new Label("Gestao de Aluguer de Vending Machines");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label userLabel = new Label(BackOfficeSession.getUsername() + "  [" + role.getLabel() + "]");
        userLabel.setStyle(theme.roleLabelStyle());

        Button logoutBtn = new Button("Sair");
        logoutBtn.setStyle(theme.switchButtonStyle());
        logoutBtn.setOnAction(e -> onLogout.run());

        HBox headerRight = new HBox(10, userLabel, logoutBtn);
        headerRight.setAlignment(Pos.CENTER_RIGHT);

        BorderPane headerPane = new BorderPane();
        headerPane.setLeft(title);
        headerPane.setRight(headerRight);
        BorderPane.setAlignment(title,       Pos.CENTER_LEFT);
        BorderPane.setAlignment(headerRight, Pos.CENTER_RIGHT);

        // ── Navigation menu ───────────────────────────────────────────────────
        HBox menu = new HBox(10);
        menu.setPadding(new Insets(6, 0, 0, 0));

        if (role == BackofficeRole.TECNICO) {
            menu.getChildren().add(
                    navBtn(theme, "Instalações", () -> {
                        TecnicoInstalacaoView v = new TecnicoInstalacaoView(stage);
                        root.setCenter(v.getRoot());
                    })
            );
        } else if (role == BackofficeRole.RECECIONISTA) {
            menu.getChildren().addAll(
                    navBtn(theme, "Gerir Clientes", () -> {
                        ClienteDesktopView v = new ClienteDesktopView(stage, role);
                        root.setCenter(v.getRoot());
                    }),
                    navBtn(theme, "Ver Contratos", () -> {
                        ContratoDesktopView v = new ContratoDesktopView(stage, true);
                        root.setCenter(v.getRoot());
                    }),
                    navBtn(theme, "Ver Propostas", () -> {
                        PropostaDesktopView v = new PropostaDesktopView(stage, true);
                        root.setCenter(v.getRoot());
                    }),
                    navBtn(theme, "Pedidos de Conta", () -> {
                        AccountRequestView v = new AccountRequestView();
                        root.setCenter(v.getRoot());
                    })
            );
        } else {
            // ADMIN + GESTOR
            if (role == BackofficeRole.ADMIN) {
                menu.getChildren().addAll(
                        navBtn(theme, "Gerir Clientes", () -> {
                            ClienteDesktopView v = new ClienteDesktopView(stage, role);
                            root.setCenter(v.getRoot());
                        }),
                        navBtn(theme, "Gerir Vending Machines", () -> {
                            VendingMachineDesktopView v = new VendingMachineDesktopView(stage);
                            root.setCenter(v.getRoot());
                        })
                );
            }
            menu.getChildren().addAll(
                    navBtn(theme, "Gerir Propostas", () -> {
                        PropostaDesktopView v = new PropostaDesktopView(stage);
                        root.setCenter(v.getRoot());
                    }),
                    navBtn(theme, "Gerir Contratos", () -> {
                        ContratoDesktopView v = new ContratoDesktopView(stage);
                        root.setCenter(v.getRoot());
                    }),
                    navBtn(theme, "Gerir Instalacoes", () -> {
                        InstalacaoDesktopView v = new InstalacaoDesktopView(stage);
                        root.setCenter(v.getRoot());
                    }),
                    destructiveNavBtn("Pedidos Rescisão", () -> {
                        PedidoRescisaoDesktopView v = new PedidoRescisaoDesktopView(stage);
                        root.setCenter(v.getRoot());
                    }),
                    navBtn(theme, "Pedidos de Conta", () -> {
                        AccountRequestView v = new AccountRequestView();
                        root.setCenter(v.getRoot());
                    })
            );
            if (role == BackofficeRole.ADMIN) {
                menu.getChildren().addAll(
                        navBtn(theme, "Audit Logs", () -> {
                            AuditLogDesktopView v = new AuditLogDesktopView();
                            root.setCenter(v.getRoot());
                        }),
                        navBtn(theme, "Utilizadores", () -> {
                            UserManagementView v = new UserManagementView();
                            root.setCenter(v.getRoot());
                        })
                );
            }
        }

        VBox top = new VBox(8, headerPane, menu);
        top.setPadding(new Insets(10));
        top.setStyle(theme.headerBarStyle());
        root.setTop(top);
    }

    private void showHome() {
        if (role == BackofficeRole.TECNICO) {
            loadView("Instalações", () -> {
                TecnicoInstalacaoView v = new TecnicoInstalacaoView(stage);
                root.setCenter(v.getRoot());
            });
        } else {
            Label welcome = new Label("Bem-vindo, " + BackOfficeSession.getUsername()
                    + ". Selecione uma opção no menu.");
            welcome.setStyle("-fx-font-size: 13px; -fx-padding: 20; -fx-text-fill: #555;");
            root.setCenter(welcome);
        }
    }

    // ── Button helpers ────────────────────────────────────────────────────────

    private Button navBtn(RoleTheme theme, String label, Runnable action) {
        Button btn = new Button(label);
        btn.setStyle(theme.navButtonStyle());
        btn.setOnAction(e -> loadView(label, action));
        return btn;
    }

    private Button destructiveNavBtn(String label, Runnable action) {
        Button btn = new Button(label);
        btn.setStyle("-fx-base: #c0392b; -fx-text-fill: white; -fx-font-weight: bold;");
        btn.setOnAction(e -> loadView(label, action));
        return btn;
    }

    private void loadView(String viewName, Runnable loader) {
        try {
            loader.run();
        } catch (Exception ex) {
            ex.printStackTrace();
            Label error = new Label("Failed to load " + viewName + ": " + ex.getMessage());
            error.setStyle("-fx-text-fill: red; -fx-padding: 20;");
            root.setCenter(error);
        }
    }
}
