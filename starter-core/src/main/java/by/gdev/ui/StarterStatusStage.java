package by.gdev.ui;

import by.gdev.http.upload.download.downloader.DownloaderStatus;
import by.gdev.http.upload.download.downloader.DownloaderStatusEnum;
import by.gdev.util.DesktopUtil;
import by.gdev.util.OSInfo;
import com.google.common.eventbus.Subscribe;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.ResourceBundle;

@Slf4j
public class StarterStatusStage extends Stage {
    private static final long serialVersionUID = 1L;
    private ProgressBar progressBar;
    private String gdevBy = "https://github.com/gdevby/starter4j";
    private Label uploadStatus = new Label("upload status");
    private ResourceBundle resourceBundle;

    public StarterStatusStage(String appName, boolean indeterminate, ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
        setResizable(false);
        initStyle(StageStyle.UNDECORATED);
        StageUtils.setFavicons(this);

        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getBounds();
        double width = bounds.getWidth() * screen.getOutputScaleX();
        double height = bounds.getHeight() * screen.getOutputScaleY();
        setWidth(width / 5);
        setHeight(height / 5);

        BorderPane borderPane = new BorderPane();

        Image image = getImage();
        if (Objects.nonNull(image)) {
            BackgroundImage bgImg = new BackgroundImage(
                    image,
                    BackgroundRepeat.ROUND,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER,
                    new BackgroundSize(100, 100, true, false, true, false)
            );
            borderPane.setBackground(new Background(bgImg));
        }

        Label label = new Label("app launcher gdev.by");
        label.setPadding(new Insets(5));
        Font font = label.getFont();
        Font newFont = Font.font(font.getFamily(), font.getSize() - 2);
        label.setFont(newFont);
        label.setStyle("-fx-font-weight: bold");

        Label nameLabel = new Label(appName);
        Font nameFont = Font.font(font.getFamily(), font.getSize() + 3);
        nameLabel.setFont(nameFont);
        nameLabel.setStyle("-fx-font-weight: bold");
        nameLabel.setAlignment(Pos.CENTER);
        nameLabel.setPadding(new Insets(0, 0 , getHeight() / 3 , 0));

        uploadStatus.setStyle("-fx-font-weight: bold");
        uploadStatus.setAlignment(Pos.CENTER_RIGHT);
        uploadStatus.setPadding(new Insets(0,3,3,0));
        uploadStatus.setPrefWidth(getWidth());

        progressBar = new ProgressBar();
        progressBar.setPrefWidth(getWidth());
        progressBar.setPadding(Insets.EMPTY);
        progressBar.setProgress(indeterminate ? ProgressBar.INDETERMINATE_PROGRESS : 0);
        progressBar.setStyle("-fx-accent: #04b424");

        borderPane.setTop(label);
        borderPane.setCenter(nameLabel);
        borderPane.setBottom(uploadStatus);

        BorderPane content = new BorderPane();
        content.setCenter(borderPane);
        content.setBottom(progressBar);

        setX(width / 2 - getWidth());
        setY(height / 2 - getHeight());

        Scene scene = new Scene(content);
        setScene(scene);

        label.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                DesktopUtil.openLink(OSInfo.getOSType(), gdevBy);
            }
        });

        label.setOnMouseEntered(event -> label.setCursor(Cursor.HAND));

        label.setOnMouseExited(event -> label.setCursor(Cursor.DEFAULT));

    }

    private Image getImage() {
        try (InputStream inputStream = StarterStatusStage.class.getResourceAsStream("/background.jpg")) {
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
        uploadStatus.setText(String.format("%s %s/%s %s ", resourceBundle.getString("uploading"), uploaded, allUpload,
                resourceBundle.getString("mb")));
        progressBar.setProgress((double) uploaded / allUpload);
    }

}
