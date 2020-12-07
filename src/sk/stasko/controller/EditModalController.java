package sk.stasko.controller;

import javafx.stage.Stage;
import sk.stasko.model.realEstate.RealEstate;
import sk.stasko.service.ServiceImpl;
import sk.stasko.util.AlertHandler;
import sk.stasko.util.Helper;

import java.io.IOException;

public class EditModalController extends AbstractController<RealEstate> {

    private Stage stage;

    protected void setFields(RealEstate item, Stage stage) {
        this.stage = stage;
        this.item = item;
        this.id.setText(String.valueOf(item.getId()));
        this.catalogNumber.setText(String.valueOf(item.getCatalogNumber()));
        this.desc.setText(item.getDescription());
        this.lon.setText(String.valueOf(item.getGps().getLongitude()));
        this.lat.setText(String.valueOf(item.getGps().getLatitude()));
    }

    public void delete() throws IOException {
        this.stage.close();
        if (this.item == null) {
            AlertHandler.errorDialog("Error", "You did not select item");
            return;
        }
        if (ServiceImpl.getInstance().delete(this.item)) {
            this.clearFields();
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
        RealEstate estate = Helper.handleRealEstate(this.catalogNumber.getText(), this.desc.getText(), this.lat.getText(), this.lon.getText());
        if (estate == null) {
            return;
        }
        ServiceImpl.getInstance().delete(this.item);
        this.clearFields();
        ServiceImpl.getInstance().add(estate);
        AlertHandler.confirmationDialog("Success" , "You have successfully edited estate", this.stage);
    }
}
