package by.gdev;

import javafx.application.Application;
import javafx.stage.Stage;

public class MainJFX extends Application {
    public static void main(String[] args) throws Exception {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        new Thread(() -> {
            try {
                Main.main(getParameters().getRaw().toArray(new String[0]));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();
    }
}
