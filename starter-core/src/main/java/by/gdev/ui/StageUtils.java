package by.gdev.ui;

import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class StageUtils {
    public static void setFavicons(Stage stage){
        try {
            List<Image> favicons = new ArrayList<>();
            int[] sizes = new int[] { 256, 128, 96, 64, 48, 32, 24, 16 };
            StringBuilder loadedBuilder = new StringBuilder();
            for (int i : sizes) {
                try (InputStream inputStream = StarterStatusStage.class.getResourceAsStream("/logo.jpg")) {
                    if (inputStream == null)
                        continue;
                    Image image = new Image(inputStream);
                    if (image.isError())
                        continue;
                    loadedBuilder.append(", ").append(i).append("px");
                    favicons.add(image);
                }
            }
            String loaded = loadedBuilder.toString();
            if (loaded.isEmpty())
                log.info("No favicon is loaded.");
            else
                log.info("Favicons loaded: {}", loaded.substring(2));
            stage.getIcons().addAll(favicons);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
