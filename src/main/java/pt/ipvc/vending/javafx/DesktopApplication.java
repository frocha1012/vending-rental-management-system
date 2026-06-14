package pt.ipvc.vending.javafx;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import pt.ipvc.vending.domain.enums.BackOfficeRole;
import pt.ipvc.vending.service.AuditContext;

public class DesktopApplication extends Application {

    private static final double WIDTH  = 1280;
    private static final double HEIGHT = 720;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Vending Rental — Backoffice");
        showLogin(stage);
        stage.show();
    }

    private void showLogin(Stage stage) {
        LoginDesktopView loginView = new LoginDesktopView(role -> showMainView(stage, role));
        stage.setScene(new Scene(loginView.getRoot(), WIDTH, HEIGHT));
        stage.setTitle("Vending Rental — Backoffice");
    }

    private void showMainView(Stage stage, BackOfficeRole role) {
        AuditContext.setActor(role.name(), BackOfficeSession.getUsername());
        RoleTheme.setCurrent(RoleTheme.forRole(role));

        DesktopMainView mainView = new DesktopMainView(
                stage,
                BackofficeRole.fromDomain(role),
                () -> {
                    BackOfficeSession.logout();
                    AuditContext.clear();
                    showLogin(stage);
                });

        stage.setScene(new Scene(mainView.getRoot(), WIDTH, HEIGHT));
        stage.setTitle("Vending Rental — Backoffice  ["
                + BackOfficeSession.getUsername() + " · " + role.getLabel() + "]");
    }

    @Override
    public void stop() {
        DesktopLauncher.getSpringContext().close();
    }
}
