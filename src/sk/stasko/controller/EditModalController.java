package sk.stasko.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import sk.stasko.model.gps.Gps;
import sk.stasko.model.realEstate.RealEstate;
import sk.stasko.service.ServiceImpl;
import sk.stasko.util.AlertHandler;
import sk.stasko.util.Helper;

import java.io.IOException;

public class EditModalController extends AbstractController<RealEstate> {
    @FXML private TextField idRealEstate;
    private Stage stage;

    protected void setFields(RealEstate item, Stage stage) {
        this.stage = stage;
        this.item = item;
        this.idRealEstate.setText(String.valueOf(item.getId()));
        this.catalogNumber.setText(String.valueOf(item.getCatalogNumber()));
        this.desc.setText(item.getDescription());
        this.lon.setText(String.valueOf(item.getGps().getLongitude()));
        this.lat.setText(String.valueOf(item.getGps().getLatitude()));
    }

    protected void clearFieldsEdit() {
        this.idRealEstate.setText("");
        this.lon.setText("");
        this.lat.setText("");
        this.desc.setText("");
        this.catalogNumber.setText("");
    }

    public void delete() throws IOException {
        this.stage.close();
        if (this.item == null) {
            AlertHandler.errorDialog("Error", "You did not select item");
            return;
        }
        if (ServiceImpl.getInstance().delete(this.item)) {
            this.clearFieldsEdit();
            AlertHandler.confirmationDialog("Success","You have deleted Real Estate", this.stage);
        } else {
            AlertHandler.confirmationDialog("Error","You have not deleted Real Estate", this.stage);
        }
    }

    public void edit() throws IOException {
        if (this.item == null) {
            AlertHandler.errorDialog("Error", "You did not select item");
            return;
        }
        int id = Integer.parseInt(this.idRealEstate.getText());
        RealEstate t = ServiceImpl.getInstance().find(id);
        if (t == null) {
            RealEstate estate = Helper.handleRealEstate(this.idRealEstate.getText(), this.catalogNumber.getText(), this.desc.getText(), this.lat.getText(), this.lon.getText());
            if (estate == null) {
                return;
            }
            ServiceImpl.getInstance().delete(this.item);
            this.clearFieldsEdit();
            ServiceImpl.getInstance().add(estate);
            AlertHandler.confirmationDialog("Success", "You have successfully edited estate", this.stage);
        } else {
            t.setCatalogNumber(Integer.parseInt(this.catalogNumber.getText()));
            t.setDescription(this.desc.getText());
            t.setGps(new Gps(Double.parseDouble(this.lat.getText()), Double.parseDouble(this.lon.getText())));
            ServiceImpl.getInstance().edit(t);
        }
    }
}
