package by.gdev.ui.subscriber;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.zip.GZIPOutputStream;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.util.EntityUtils;

import com.google.common.eventbus.Subscribe;

import by.gdev.Main;
import by.gdev.http.download.exeption.HashSumAndSizeError;
import by.gdev.http.download.exeption.UploadFileException;
import by.gdev.http.upload.download.downloader.DownloaderStatus;
import by.gdev.http.upload.download.downloader.DownloaderStatusEnum;
import by.gdev.model.ExceptionMessage;
import by.gdev.model.LogResponse;
import by.gdev.model.StarterAppConfig;
import by.gdev.model.StarterAppProcess;
import by.gdev.ui.StarterStatusStage;
import by.gdev.util.CoreUtil;
import by.gdev.util.DesktopUtil;
import by.gdev.util.OSInfo.OSType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
public class ViewSubscriber {

	private StarterStatusStage stage;
	private ResourceBundle bundle;
	private OSType osType;
	private StarterAppConfig starterConfig;

	@Subscribe
	private void procces(StarterAppProcess status) {
		if (!StringUtils.isEmpty(status.getLine())
				&& status.getLine().equals("java.lang.UnsatisfiedLinkError: no zip in java.library.path")) {
			message(new ExceptionMessage(String.format(bundle.getString("unsatisfied.link.error"),
					Paths.get(starterConfig.getWorkDirectory()).toAbsolutePath().toString(),
					"C:\\" + starterConfig.getWorkDirectory())));
		}
		if (Objects.nonNull(status.getErrorCode())) {
			if (status.getErrorCode() == -1073740791) {
				message(new ExceptionMessage(bundle.getString("driver.error"),
						"https://gdev.by/help/java/closed-1073740791"));
			} else if (status.getErrorCode() == -1073740771)
				message(new ExceptionMessage(bundle.getString("msi.afterburner.error")));
			else if (status.getErrorCode() != 0) {
				String s1 = Objects.nonNull(starterConfig.getLogURIService()) ? "unidentified.error"
						: "unidentified.error.1";
				ExceptionMessage e = new ExceptionMessage(bundle.getString(s1));
				e.setLogButton(true);
				message(e);
				Platform.runLater(() -> System.exit(0));
			}
		}
	}

	@Subscribe
	public void message(DownloaderStatus status) {
		if (DownloaderStatusEnum.DONE.equals(status.getDownloaderStatusEnum())) {
			if (!status.getThrowables().isEmpty()) {
				Throwable t = status.getThrowables().get(0);
				if (t instanceof HashSumAndSizeError) {
					HashSumAndSizeError t1 = (HashSumAndSizeError) t;
					String s = String.format(bundle.getString("upload.error.hash.sum"), t1.getUri(), t1.getMessage());
					message(new ExceptionMessage(s, t1.getUri()));
				} else if (t instanceof UploadFileException) {
					message(new ExceptionMessage(bundle.getString("net.problem")));
				}
			}
		}
	}

	@Subscribe
	public void message(ExceptionMessage s) {
		CompletableFuture<BorderPane> borderPaneFuture = new CompletableFuture<>();
		Platform.runLater(() -> {
			TextArea textArea = getTextAreaWithMessage(s);
			BorderPane borderPane = new BorderPane();
			borderPane.setCenter(textArea);
			if (s.isLogButton() && Objects.nonNull(starterConfig.getLogURIService()))
				addLogOffer(borderPane);
			if (Objects.nonNull(s.getLink())) {
				textArea.setOnMouseClicked(event -> {
					if (event.getButton() == MouseButton.PRIMARY) {
						CoreUtil.openLink(s.getLink());
					}
				});
			}
			borderPaneFuture.complete(borderPane);
		});
		BorderPane borderPane = null;
		try {
			borderPane = borderPaneFuture.get();
		} catch (InterruptedException | ExecutionException e) {
			log.error(e.getMessage(), e);
		}
		showMessageDialog(stage, borderPane, "", s.getMessage(), Alert.AlertType.ERROR);
	}

	protected void addLogOffer(BorderPane borderPane) {
		StackPane stackPane = new StackPane();
		Button button = new Button(bundle.getString("link.get"));
		Label label = new Label(bundle.getString("preparing"));
		TextArea textArea = getTextAreaWithMessage(new ExceptionMessage(""));
		stackPane.getChildren().add(textArea);
		stackPane.getChildren().add(button);
		stackPane.getChildren().add(label);
		textArea.setVisible(true);
		label.setVisible(false);
		button.setOnAction(event -> {
			button.setVisible(false);
			label.setVisible(true);
			doRequest(stackPane, label, textArea);
		});
		textArea.setPadding(new Insets(5));
		borderPane.setBottom(stackPane);
	}

	private void doRequest(StackPane stackPane, Label label, TextArea textArea) {
		CompletableFuture.runAsync(() -> {
			Exception e2 = null;
			Pair<String, byte[]> pair = null;
			for (String s : starterConfig.getLogURIService()) {
				HttpPost method = new HttpPost(s);
				CloseableHttpResponse response = null;
				try {
					pair = CoreUtil.readFileLog();
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					GZIPOutputStream g = new GZIPOutputStream(out);
					g.write(pair.getValue());
					g.close();
					byte[] body = out.toByteArray();
					method.setEntity(new ByteArrayEntity(body));
					method.setConfig(RequestConfig.custom().setConnectTimeout(5000).setSocketTimeout(60000).build());
					response = Main.client.execute(method);
					if (response.getStatusLine().getStatusCode() >= 300) {
						log.info("not proper code " + response.getStatusLine().toString());
						showError(stackPane, pair);
					} else {
						LogResponse lr = Main.GSON.fromJson(
								IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8),
								LogResponse.class);
						Platform.runLater(() -> {
							textArea.setText(lr.getLink());
							Clipboard clipboard1 = Clipboard.getSystemClipboard();
							ClipboardContent content1 = new ClipboardContent();
							content1.putString(lr.getLink());
							clipboard1.setContent(content1);
							label.setVisible(false);
							textArea.setVisible(true);
							showMessageDialog(stage, new Label(bundle.getString("clipboard.copy")), "","",
									Alert.AlertType.INFORMATION);
						});
					}
					return;
				} catch (Exception e1) {
					e2 = e1;
				} finally {
					if (Objects.nonNull(response)) {
						method.abort();
						EntityUtils.consumeQuietly(response.getEntity());
					}
				}
			}
			log.error("exception", e2);
			showError(stackPane, pair);
		});
	}

	private void showError(StackPane stackPane, Pair<String, byte[]> pair) {
		ExceptionMessage e = new ExceptionMessage(
				String.format(bundle.getString("error.log.send"), Objects.isNull(pair) ? "" : pair.getKey()), "");
		TextArea textArea = getTextAreaWithMessage(e);
		Platform.runLater(() -> stackPane.setVisible(false));
		showMessageDialog(stage, textArea, null, e.getMessage(), Alert.AlertType.ERROR);
	}

	protected TextArea getTextAreaWithMessage(ExceptionMessage s) {
		String message = ExceptionUtils.getStackTrace(s.getError());
		int rowCount = message.split("\n").length;
		int columnCount = message.length() / rowCount;

		TextArea textArea = new TextArea(message);
		textArea.setPrefRowCount(rowCount + 3);
		textArea.setPrefColumnCount(columnCount);
		textArea.setEditable(false);
		textArea.setBackground(null);
		textArea.setBorder(null);

		return textArea;
	}

	protected void showMessageDialog(Stage stage, Node content, String title, String header, Alert.AlertType type) {

		CountDownLatch latch = new CountDownLatch(1);

		Platform.runLater(() -> {
			Alert alert = new Alert(type);
			alert.setTitle(title);
			alert.setHeaderText(header);
			alert.initOwner(stage);
			alert.getDialogPane().setContent(content);
			alert.showAndWait();

			latch.countDown();
		});

		try {
			latch.await();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
