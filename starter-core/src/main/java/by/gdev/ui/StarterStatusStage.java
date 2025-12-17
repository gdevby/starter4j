package by.gdev.ui;

import by.gdev.http.upload.download.downloader.DownloaderStatus;
import by.gdev.http.upload.download.downloader.DownloaderStatusEnum;
import com.google.common.eventbus.Subscribe;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

@Slf4j
public class StarterStatusStage extends Stage {
    private static final long serialVersionUID = 1L;
    private ProgressBar progressBar;
    private Label uploadStatus = new Label();
    private Label uploadWord = new Label();
    private Label uploadPercent = new Label();
    private ResourceBundle resourceBundle;

    public StarterStatusStage(String appName, boolean indeterminate, ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
        setResizable(false);
        initStyle(StageStyle.TRANSPARENT);
        StageUtils.setFavicons(this);

        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getBounds();
        double width = bounds.getWidth() * screen.getOutputScaleX();
        double height = bounds.getHeight() * screen.getOutputScaleY();
        setMinWidth(width / 5);
        setMinHeight(height / 5);

        setOnShown(event -> {
            setX(width / 2 - getWidth());
            setY(height / 2 - getHeight());
        });

        VBox root = new VBox();
        root.setSpacing(15);
        root.setPadding(new Insets(35, 35, 35, 35));
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("root-status");

        Image image = getImage();
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(100);
        imageView.setFitHeight(100);

        Label nameLabel = new Label(appName);
        nameLabel.setPadding(new Insets(0, 0 , 20, 0));

        VBox downloadVBox = new VBox();
        downloadVBox.setSpacing(5);

        AnchorPane anchorPane = new AnchorPane();
        AnchorPane.setLeftAnchor(uploadWord, 0.0);

        AnchorPane.setRightAnchor(uploadPercent, 0.0);
        anchorPane.getChildren().addAll(uploadWord, uploadPercent);

        uploadStatus.setMaxWidth(Double.MAX_VALUE);
        uploadStatus.setAlignment(Pos.CENTER_RIGHT);

        progressBar = new ProgressBar();
        progressBar.setPadding(Insets.EMPTY);
        progressBar.setProgress(indeterminate ? ProgressBar.INDETERMINATE_PROGRESS : 0);
        progressBar.setMaxWidth(Double.MAX_VALUE);

        downloadVBox.getChildren().addAll(anchorPane, progressBar, uploadStatus);

        root.getChildren().addAll(imageView, nameLabel, downloadVBox);

        Scene scene = new Scene(root, Color.TRANSPARENT);
        URL stylesheet = getClass().getResource("/style.css");
        if (Objects.nonNull(stylesheet))
            scene.getStylesheets().add(stylesheet.toExternalForm());
        setScene(scene);
    }

    private Image getImage() {
        try (InputStream inputStream = StarterStatusStage.class.getResourceAsStream("/big-logo.png")) {
            if (inputStream == null) return null;
            return new Image(inputStream);
        } catch (IOException e) {
            log.warn("can't load image", e);
        }
        return null;
    }

    @Subscribe
    public void messageToSpeed(DownloaderStatus status) {
        Platform.runLater(() -> {
            if (progressBar.getProgress() == ProgressIndicator.INDETERMINATE_PROGRESS && DownloaderStatusEnum.WORK.equals(status.getDownloaderStatusEnum())) {
                progressBar.setProgress(0);
                updateUploadProgressBar(status);
            } else if (progressBar.getProgress() != ProgressBar.INDETERMINATE_PROGRESS) {
                updateUploadProgressBar(status);
            }
        });
    }

    private void updateUploadProgressBar(DownloaderStatus status) {
        int uploaded = (int) status.getDownloadSize() / (1024 * 1024);
        int allUpload = (int) status.getAllDownloadSize() / (1024 * 1024);
        double progress = (double) uploaded / allUpload;
        uploadPercent.setText((int)(progress * 100) + "%");
        uploadWord.setText(resourceBundle.getString("uploading"));
        uploadStatus.setText(String.format("%s/%s %s ", uploaded, allUpload, resourceBundle.getString("mb")));
        progressBar.setProgress(progress);
    }

}
