package pt.ipvc.vending.javafx;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import pt.ipvc.vending.domain.entity.BackOfficeUser;
import pt.ipvc.vending.domain.enums.BackOfficeRole;
import pt.ipvc.vending.service.BackOfficeUserService;

import java.util.function.Consumer;

public class LoginDesktopView {

    private final VBox root = new VBox(20);

    public LoginDesktopView(Consumer<BackOfficeRole> onLoginSuccess) {
        buildLayout(onLoginSuccess);
    }

    public VBox getRoot() {
        return root;
    }

    private void buildLayout(Consumer<BackOfficeRole> onLoginSuccess) {
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(60));
        root.setStyle("-fx-background-color: #f4f4f4;");
        root.setMaxWidth(400);

        Label title = new Label("Vending Rental");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label subtitle = new Label("Backoffice — Autenticação");
        subtitle.setStyle("-fx-font-size: 15px; -fx-text-fill: #555;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setPrefWidth(280);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setPrefWidth(280);

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #c0392b; -fx-font-size: 12px;");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        Button loginBtn = new Button("Entrar");
        loginBtn.setPrefSize(280, 40);
        loginBtn.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; "
                + "-fx-base: #2980b9; -fx-text-fill: white;");

        Runnable doLogin = () -> {
            String u = usernameField.getText().trim();
            String p = passwordField.getText();
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);

            if (u.isEmpty() || p.isEmpty()) {
                errorLabel.setText("Preencha o username e a password.");
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
                return;
            }

            try {
                BackOfficeUserService svc =
                        DesktopLauncher.getSpringContext().getBean(BackOfficeUserService.class);
                BackOfficeUser user = svc.authenticate(u, p);

                BackOfficeSession.login(user.getId(), user.getUsername(), user.getRole());
                RoleTheme.setCurrent(RoleTheme.forRole(user.getRole()));
                onLoginSuccess.accept(user.getRole());

            } catch (IllegalArgumentException ex) {
                errorLabel.setText(ex.getMessage());
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
                passwordField.clear();
            } catch (Exception ex) {
                errorLabel.setText("Erro inesperado. Tente novamente.");
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
            }
        };

        loginBtn.setOnAction(e -> doLogin.run());
        passwordField.setOnAction(e -> doLogin.run());

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(12);
        form.setAlignment(Pos.CENTER);
        form.addRow(0, new Label("Username:"), usernameField);
        form.addRow(1, new Label("Password:"), passwordField);

        root.getChildren().addAll(title, subtitle, form, loginBtn, errorLabel);
    }
}
