package sk.stasko;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sk.stasko.core.fileHandler.FileHandler;
import sk.stasko.model.realEstate.RealEstate;
import sk.stasko.model.realEstate.RealEstateFileHandler;
import sk.stasko.service.ServiceImpl;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * TODO riadici blok , refactor directory class a overflowing class, zmena map na listy, odstranit duplicity pri vypise, refaktor citania blokov vo vypise
 */
public class Main extends Application {
    private static RandomAccessFile randomAccessFileMain;
    private static RandomAccessFile randomAccessFileOver;

    @Override
    public void init() throws Exception {
        randomAccessFileMain = new RandomAccessFile("main.dat", "rw");
        randomAccessFileOver = new RandomAccessFile("over.dat", "rw");
    }

    /**
     *
     * @param stage - current stage of application
     * @throws IOException - exception that are throwing if something happened during work with file
     */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("System.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
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
        return false;
    }

    public static void main(String[] args) {
        try {
            randomAccessFileMain = new RandomAccessFile("main.dat", "rw");
            randomAccessFileOver = new RandomAccessFile("over.dat", "rw");
            FileHandler<RealEstate> fileHandlerMain = new RealEstateFileHandler(randomAccessFileMain);
            FileHandler<RealEstate> over = new RealEstateFileHandler(randomAccessFileOver);
            ServiceImpl.setInstance(fileHandlerMain, over);
            launch();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.getStackTrace();
        }
    }
}