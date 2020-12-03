package sk.stasko.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public abstract class AbstractController<T> {
    @FXML protected TextArea desc;
    @FXML protected TextArea mainPart;
    @FXML protected TextArea mainPartBlank;
    @FXML protected TextArea overflowPart;
    @FXML protected TextArea overflowPartBlank;
    @FXML protected TextField lon;
    @FXML protected TextField lat;
    @FXML protected TextField id;
    @FXML protected TextField catalogNumber;

    protected T item;

    protected void clearFields() {
        this.id.setText("");
        this.lon.setText("");
        this.lat.setText("");
        this.desc.setText("");
        this.catalogNumber.setText("");
    }

    public void exit() {
        System.exit(-1);
    }
}
