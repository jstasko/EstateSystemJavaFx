package sk.stasko.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import sk.stasko.Main;
import sk.stasko.core.fileHandler.FileHandler;
import sk.stasko.model.realEstate.RealEstate;
import sk.stasko.model.realEstate.RealEstateFileHandler;
import sk.stasko.service.ServiceImpl;
import sk.stasko.util.AlertHandler;

import java.io.IOException;

public class MainController extends AbstractController<RealEstate> {
    @FXML private TextField maxItem;
    @FXML private TextField allowedBits;
    @FXML private TextField maxItemOverflow;
    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void load() throws IOException {
        FileHandler<RealEstate> fileHandlerMain = new RealEstateFileHandler(Main.randomAccessFileMain);
        FileHandler<RealEstate> over = new RealEstateFileHandler(Main.randomAccessFileOver);
        ServiceImpl.setInstance(fileHandlerMain, over);
        Main.setNewRoot("System");
    }

    public void createNew() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("view/modal/CreateModal.fxml"));
        Parent mainLoader = fxmlLoader.load();
        MainController mainController = fxmlLoader.getController();
        Stage stage = new Stage();
        stage.setTitle("Create new");
        stage.setScene(new Scene(mainLoader, 400, 350));
        mainController.setStage(stage);
        stage.show();
    }

    public void getNew() throws IOException {
        int maxItem;
        int allowedBits;
        int maxItemOverflow;
        try {
            maxItem = Integer.parseInt(this.maxItem.getText());
            allowedBits = Integer.parseInt(this.allowedBits.getText());
            maxItemOverflow = Integer.parseInt(this.maxItemOverflow.getText());
        } catch (NumberFormatException e) {
            AlertHandler.errorDialog("Error", "You did not type number");
            return;
        }


        FileHandler<RealEstate> fileHandlerMain = new RealEstateFileHandler(Main.randomAccessFileMain);
        FileHandler<RealEstate> over = new RealEstateFileHandler(Main.randomAccessFileOver);
        ServiceImpl.setInstance(maxItem, allowedBits, maxItemOverflow, fileHandlerMain, over);
        Main.setNewRoot("System");
        stage.close();
        this.stage = null;
    }
}
