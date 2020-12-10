package sk.stasko.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import sk.stasko.AppGen;
import sk.stasko.util.AlertHandler;

import java.io.IOException;
import java.util.Random;

public class GeneratorModalController extends AbstractController {
    @FXML private TextField realEstatesNumber;
    private Stage stage;

    public void setState(Stage stage) {
        this.stage = stage;
    }

    public void generateObjects() throws IOException {
        int numberRealEstates;
        try {
            numberRealEstates = Integer.parseInt(this.realEstatesNumber.getText());
        } catch (NumberFormatException e) {
            AlertHandler.errorDialog("Error", "You did not type a number");
            return;
        }
        if (numberRealEstates < 0) {
            AlertHandler.errorDialog("Error", "You can not type negative number");
            return;
        }
        Random random = new Random();
        AppGen.generateRealEstates(numberRealEstates, random);
        AlertHandler.confirmationDialog("Success", "You have successfully generated objects", stage);
    }
}
