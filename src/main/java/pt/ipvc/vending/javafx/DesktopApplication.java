package pt.ipvc.vending.javafx;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class DesktopApplication extends Application {

    private static final double WIDTH  = 960;
    private static final double HEIGHT = 600;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Vending Rental — Backoffice");
        showRoleSelection(stage);
        stage.show();
    }

    private void showRoleSelection(Stage stage) {
        RoleSelectionView selectionView = new RoleSelectionView(
                role -> showMainView(stage, role));
        stage.setScene(new Scene(selectionView.getRoot(), WIDTH, HEIGHT));
        stage.setTitle("Vending Rental — Backoffice");
    }

    private void showMainView(Stage stage, BackofficeRole role) {
        RoleTheme.setCurrent(RoleTheme.forRole(role));
        DesktopMainView mainView = new DesktopMainView(
                stage, role, () -> showRoleSelection(stage));
        stage.setScene(new Scene(mainView.getRoot(), WIDTH, HEIGHT));
        stage.setTitle("Vending Rental — Backoffice  [" + role.getLabel() + "]");
    }

    @Override
    public void stop() {
        DesktopLauncher.getSpringContext().close();
    }
}
