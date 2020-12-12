package sk.stasko.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import sk.stasko.Main;
import sk.stasko.model.realEstate.RealEstate;
import sk.stasko.service.ServiceImpl;
import sk.stasko.util.AlertHandler;
import sk.stasko.util.Helper;

import java.io.IOException;

public class SystemController extends AbstractController<RealEstate> {
    @FXML private TextField idRealEstate;
    @FXML private ListView<RealEstate> realEstateView;

    public void handleSelect() throws IOException {
        RealEstate estate = this.realEstateView.getSelectionModel().getSelectedItem();
        if (estate == null) {
            AlertHandler.errorDialog("Error", "You did not correctly select item");
            return;
        }
        this.realEstateView.getItems().clear();
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("view/modal/EditModal.fxml"));
        Parent mainLoader = fxmlLoader.load();
        EditModalController editController = fxmlLoader.getController();
        Stage stage = new Stage();
        stage.setTitle("Edit/Delete");
        stage.setScene(new Scene(mainLoader, 400, 350));
        editController.setFields(estate, stage);
        stage.show();
    }

    public void handleGenerator() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("view/modal/GenModal.fxml"));
        Parent mainLoader = fxmlLoader.load();
        GeneratorModalController editController = fxmlLoader.getController();
        Stage stage = new Stage();
        stage.setTitle("Generator");
        stage.setScene(new Scene(mainLoader, 350, 250));
        editController.setState(stage);
        stage.show();
    }

    public void findEstate() throws IOException {
        int id;
        try {
            id = Integer.parseInt(this.id.getText());
        } catch (NumberFormatException e) {
            AlertHandler.errorDialog("Error", "You did not type correct id");
            return;
        }
        RealEstate estate = ServiceImpl.getInstance().find(id);
        if (estate == null) {
            AlertHandler.errorDialog("Error", "There is no such estate");
            return;
        }
        this.realEstateView.getItems().setAll(estate);
        this.realEstateView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }

    public void add() throws IOException {
        RealEstate estate = Helper.handleRealEstate(idRealEstate.getText(), this.catalogNumber.getText(), this.desc.getText(), this.lat.getText(), this.lon.getText());
        if (estate == null) {
            return;
        }
        ServiceImpl.getInstance().add(estate);
        AlertHandler.informationDialog("Success","You have added Real Estate with id " + estate.getId());
    }

    public void getMainPart() throws IOException {
        this.mainPart.setText(ServiceImpl.getInstance().getMainPart());
        this.mainPartBlank.setText(ServiceImpl.getInstance().getMainPartBlank());
    }

    public void getOverflowPart() throws IOException {
        this.overflowPart.setText(ServiceImpl.getInstance().getOverflowPart());
        this.overflowPartBlank.setText(ServiceImpl.getInstance().getOverflowPartBlank());
    }

    public void save() {
        if (Main.save()) {
            AlertHandler.informationDialog("Success", "You have saved your settings");
        } else {
            AlertHandler.errorDialog("Error", "Saving was not successful");
        }
    }
}
