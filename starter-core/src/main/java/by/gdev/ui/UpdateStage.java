package by.gdev.ui;

import by.gdev.model.AppConfig;
import by.gdev.model.AppLocalConfig;
import by.gdev.model.StarterAppConfig;
import by.gdev.util.DesktopUtil;
import by.gdev.util.OSInfo;
import by.gdev.utils.service.FileMapperService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class UpdateStage extends Stage {

    private static final long serialVersionUID = 2832387031191814036L;
    private AtomicInteger userChoose = new AtomicInteger();

    public UpdateStage(Stage progressStage, ResourceBundle resourceBundle, AppLocalConfig appLocalConfig,
                       AppConfig remoteAppConfig, StarterAppConfig starterAppConfig, FileMapperService fileMapperService,
                       OSInfo.OSType osType) {

        progressStage.hide();
        setResizable(false);
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getBounds();
        double width = bounds.getWidth() * screen.getOutputScaleX();
        double height = bounds.getHeight() * screen.getOutputScaleY();
        setX(width / 2 - width / 4);
        setY(height / 2 - height / 5);
        VBox vBox = new VBox();
        vBox.setStyle("-fx-background-color: rgb(215, 215, 215);");
        vBox.setPadding(new Insets(10));
        String link = starterAppConfig.getServerFile().get(0) + StarterAppConfig.APP_CHANGES_LOG;
        Label text = new Label(String.format(resourceBundle.getString("update.app"),
                appLocalConfig.getCurrentAppVersion(), remoteAppConfig.getAppVersion()).replace("<br>", "\n"));
        text.setStyle("-fx-font-weight: bold");
        text.setWrapText(true);

        HBox buttonPanel = new HBox();
        buttonPanel.setAlignment(Pos.CENTER);
        buttonPanel.setSpacing(10);

        addButton(new Button(resourceBundle.getString("skip")), buttonPanel, 1);
        addButton(new Button(resourceBundle.getString("update")), buttonPanel, 2);

        CheckBox j = new CheckBox(
                String.format(resourceBundle.getString("not.show.again"), remoteAppConfig.getAppVersion()));
        j.setAlignment(Pos.CENTER);
        j.setOnAction(e -> {
            try {
                AppLocalConfig app = fileMapperService.read(StarterAppConfig.APP_STARTER_LOCAL_CONFIG,
                        AppLocalConfig.class);
                app.setSkipUpdateVersion(remoteAppConfig.getAppVersion());
                fileMapperService.write(app, StarterAppConfig.APP_STARTER_LOCAL_CONFIG);
            } catch (IOException e1) {
                log.error("error", e1);
            }
        });

        text.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                DesktopUtil.openLink(osType, link);
            }
        });
        text.setOnMouseEntered(event -> text.setCursor(Cursor.HAND));

        text.setOnMouseExited(event -> text.setCursor(Cursor.DEFAULT));

        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(10);
        vBox.setPadding(new Insets(10));
        vBox.getChildren().addAll(text, buttonPanel, j);

        Scene scene = new Scene(vBox);
        setScene(scene);
        showAndWait();

        progressStage.show();
    }

    private void addButton(Button b, HBox panel, int code) {
        b.setAlignment(Pos.CENTER);
        b.setOnMouseClicked(event -> {
            userChoose.set(code);
            close();
        });
        panel.getChildren().add(b);
    }

    private void addButton(Button b, FlowPane panel, int code) {
        b.setAlignment(Pos.CENTER);
        b.setOnMouseClicked(event -> {
            userChoose.set(code);
            close();
        });
        panel.getChildren().add(b);
    }

    public int getUserChoose() {
        return userChoose.get();
    }

}
