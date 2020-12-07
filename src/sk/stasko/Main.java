package sk.stasko;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sk.stasko.service.ServiceImpl;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * TODO  porozmyslat nad lepsim striasanim
 */
public class Main extends Application {
    public static RandomAccessFile randomAccessFileMain;
    public static RandomAccessFile randomAccessFileOver;
    private static Scene scene;

    /**
     *
     * @param stage - current stage of application
     * @throws IOException - exception that are throwing if something happened during work with file
     */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("Main.fxml"));
        scene = new Scene(fxmlLoader.load());
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        randomAccessFileMain.close();
        randomAccessFileOver.close();
    }

    /**
     * @return boolean
     */
    public static boolean save() {
        try {
            ServiceImpl.getInstance().saveSettings();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /**
     * @param fxml - view
     */
    public static void setRoot(Parent fxml) {
        scene.setRoot(fxml);
    }

    /**
     *
     * @param newFxml - new fxml file to be load
     * @throws IOException - throws during work with file
     */
    public static void setNewRoot(String newFxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource(newFxml + ".fxml"));
        Main.setRoot(fxmlLoader.load());
    }

    public static void main(String[] args) {
        try {
            randomAccessFileMain = new RandomAccessFile("main.dat", "rw");
            randomAccessFileOver = new RandomAccessFile("over.dat", "rw");
            launch();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.getStackTrace();
        }
    }
}