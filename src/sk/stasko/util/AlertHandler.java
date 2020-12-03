package sk.stasko.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

public class AlertHandler {
    /**
     * @param title - title of dialog
     * @param content - content of dialog
     */
    public static void informationDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(title);
        alert.setTitle("Info");
        alert.setContentText(content);
        alert.show();
    }

    /**
     * @param title - title of dialog
     * @param content - content of dialog
     */
    public static void errorDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(title);
        alert.setTitle("Error");
        alert.setContentText(content);
        alert.show();
    }

    /**
     * @param title - title of dialog
     * @param content - content of dialog
     */
    public static void confirmationDialog(String title, String content, Stage stage) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(title);
        alert.setTitle("Info");
        alert.setContentText(content);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                stage.close();
            }
        });
    }
}
