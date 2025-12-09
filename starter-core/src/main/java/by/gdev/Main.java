package by.gdev;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystemException;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import org.apache.commons.io.IOExceptionList;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.google.common.eventbus.EventBus;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import by.gdev.component.Starter;
import by.gdev.handler.Localise;
import by.gdev.http.download.config.HttpClientConfig;
import by.gdev.model.ExceptionMessage;
import by.gdev.model.StarterAppConfig;
import by.gdev.subscruber.ConsoleSubscriber;
import by.gdev.ui.StarterStatusStage;
import by.gdev.ui.subscriber.ViewSubscriber;
import by.gdev.util.DesktopUtil;
import by.gdev.util.OSInfo;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {
	public static Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	public static Charset charset = StandardCharsets.UTF_8;
	public static CloseableHttpClient client;

	public static void main(String[] args) throws Exception {
		boolean flag = true;
		checkOnInvalidPath();
		System.setProperty("java.net.preferIPv4Stack", String.valueOf(flag));
		EventBus eventBus = new EventBus();
		StarterAppConfig starterConfig = StarterAppConfig.DEFAULT_CONFIG;
		JCommander.newBuilder().addObject(starterConfig).build().parse(args);
		if (Objects.isNull(starterConfig.getWorkDirectory())) {
			starterConfig.buildAbsoluteWorkDirectory(OSInfo.getOSType());
		}
		
		loadLogbackConfig(starterConfig);
		log.info("starter was run");
		log.info("starter created {}", DesktopUtil.getTime(Main.class));
		// fix for new and old client without /
		starterConfig.setServerFile(starterConfig.getServerFile().stream().map(e -> {
			if (!e.endsWith("/")) {
				log.warn("does't end with /, will add, {}", e);
				return e + "/";
			} else {
				return e;
			}
		}).collect(Collectors.toList()));
		ResourceBundle bundle = null;

		try {
			client = HttpClientConfig.getInstanceHttpClient(starterConfig.getConnectTimeout(),
					starterConfig.getSocketTimeout(), 5, 20);
			bundle = ResourceBundle.getBundle("application", new Localise().getLocal());
			StarterStatusStage starterStatusStage = null;
			if (!GraphicsEnvironment.isHeadless()) {
				ResourceBundle finalBundle = bundle;
				CompletableFuture<StarterStatusStage> starterStatusStageFuture = new CompletableFuture<>();
				Platform.runLater(() -> {
					StarterStatusStage stage = new StarterStatusStage("get installed app name", true,
							ResourceBundle.getBundle("application", new Localise().getLocal()));
					stage.show();
					eventBus.register(stage);
					eventBus.register(new ViewSubscriber(stage, finalBundle, OSInfo.getOSType(), starterConfig));
					starterStatusStageFuture.complete(stage);
				});
				starterStatusStage = starterStatusStageFuture.get();
			}
			if (starterConfig.isProd() && !starterConfig.getServerFile().equals(StarterAppConfig.URI_APP_CONFIG)) {
				String errorMessage = String.format(
						"The prod parameter is true. You don't need to change the value of the field. Current: %s, should be: %s",
						starterConfig.getServerFile(), StarterAppConfig.URI_APP_CONFIG);
				throw new RuntimeException(errorMessage);
			}
			Starter s = new Starter(eventBus, starterConfig, bundle, starterStatusStage);
			eventBus.register(new ConsoleSubscriber(bundle, s.getFileMapperService(), starterConfig));
			s.cleanCache();
			s.updateApplication();
			s.validateEnvironmentAndAppRequirements();
			s.prepareResources();
			s.runApp();

		} catch (FileSystemException | IOExceptionList ex) {
			log.error("error", ex);
			if (Objects.nonNull(bundle)) {
				eventBus.post(new ExceptionMessage(
						String.format(bundle.getString("file.delete.problem"), ex.getLocalizedMessage()),
						"https://gdev.by/help/java/check-disk"));
			}
			
		} catch (Throwable t) {
			log.error("error", t);
			String message = t.getMessage();
			if ("file doesn't exist".equals(message)) {
				eventBus.post(new ExceptionMessage(bundle.getString("download.error")));
			} else if (Objects.nonNull(message) && message.contains("GetIpAddrTable")) {
				eventBus.post(new ExceptionMessage(bundle.getString("get.ip.addr.table")));
			} else if (Objects.nonNull(message) && message.contains("CRC")) {
				eventBus.post(new ExceptionMessage(
						String.format(bundle.getString("file.delete.problem"), t.getLocalizedMessage()),
						"https://gdev.by/help/java/check-disk"));
			} else {
				String s1 = Objects.nonNull(starterConfig.getLogURIService()) ? "unidentified.error"
						: "unidentified.error.1";
				eventBus.post(new ExceptionMessage(bundle.getString(s1), t, true));
			}
			System.exit(-1);
		}
	}

	protected static void loadLogbackConfig(StarterAppConfig starterConfig) throws JoranException, IOException {
		System.setProperty("logs_dir", Paths.get(".").toAbsolutePath().toString());
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		loggerContext.reset();
		JoranConfigurator configurator = new JoranConfigurator();
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		InputStream configStream = classloader.getResourceAsStream("logbackFull.xml");
		configurator.setContext(loggerContext);
		configurator.doConfigure(configStream); // loads logback file
		configStream.close();
		log.info("logs directory {}", System.getProperty("logs_dir") + "/logs/starter/");
	}

	private static void checkOnInvalidPath() throws UnsupportedEncodingException {
		String jarFile = new File(
				URLDecoder.decode(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath(), "UTF-8"))
				.toString();
		if (jarFile.contains("!")) {
			String message = String.format("Java can't work with path that contains symbol '!', "
					+ "create new local user without characters '!'(use new local user for game) and use path without '!' characters \r\n"
					+ "current: %1$s\r\n\r\n"
					+ "Джава не работает c путями в которых содержится восклицательный знак '!' ,"
					+ " создайте новую учетную запись без '!' знаков(используйте её для игры) и используйте путь к файлу без '!'\r\n текущий: %1$s",
					jarFile);
			Platform.runLater(() -> {
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setHeaderText(null);
				alert.getDialogPane().setContent(new Label(message));
				alert.show();
			});
		}
	}
}