package pt.ipvc.vending.javafx;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

public class RoleSelectionView {

    private final VBox root = new VBox(20);

    public RoleSelectionView(Consumer<BackofficeRole> onRoleSelected) {
        buildLayout(onRoleSelected);
    }

    public VBox getRoot() {
        return root;
    }

    private void buildLayout(Consumer<BackofficeRole> onRoleSelected) {
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(60));
        root.setStyle("-fx-background-color: #f4f4f4;");

        Label title = new Label("Vending Rental");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label subtitle = new Label("Backoffice — Selecionar Perfil de Acesso");
        subtitle.setStyle("-fx-font-size: 15px; -fx-text-fill: #555;");

        Button adminBtn = new Button("Administrador");
        adminBtn.setPrefSize(200, 60);
        adminBtn.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; "
                + "-fx-base: #2980b9; -fx-text-fill: white;");
        adminBtn.setOnAction(e -> onRoleSelected.accept(BackofficeRole.ADMIN));

        Button managerBtn = new Button("Gestor");
        managerBtn.setPrefSize(200, 60);
        managerBtn.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; "
                + "-fx-base: #27ae60; -fx-text-fill: white;");
        managerBtn.setOnAction(e -> onRoleSelected.accept(BackofficeRole.MANAGER));

        Button rececionistaBtn = new Button("Rececionista");
        rececionistaBtn.setPrefSize(200, 60);
        rececionistaBtn.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; "
                + "-fx-base: #8e44ad; -fx-text-fill: white;");
        rececionistaBtn.setOnAction(e -> onRoleSelected.accept(BackofficeRole.RECECIONISTA));

        Button tecnicoBtn = new Button("Técnico");
        tecnicoBtn.setPrefSize(200, 60);
        tecnicoBtn.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; "
                + "-fx-base: #d35400; -fx-text-fill: white;");
        tecnicoBtn.setOnAction(e -> onRoleSelected.accept(BackofficeRole.TECNICO));

        HBox buttons = new HBox(30, adminBtn, managerBtn, rececionistaBtn, tecnicoBtn);
        buttons.setAlignment(Pos.CENTER);

        Label hint = new Label("Nenhuma autenticação necessária");
        hint.setStyle("-fx-font-size: 11px; -fx-text-fill: #aaa;");

        root.getChildren().addAll(title, subtitle, buttons, hint);
    }
}
