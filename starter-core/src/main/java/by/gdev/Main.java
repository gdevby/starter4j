package by.gdev;

import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystemException;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.apache.commons.io.IOExceptionList;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.google.common.eventbus.EventBus;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import by.gdev.component.Starter;
import by.gdev.handler.Localise;
import by.gdev.handler.UpdateCore;
import by.gdev.http.download.config.HttpClientConfig;
import by.gdev.model.ExceptionMessage;
import by.gdev.model.StarterAppConfig;
import by.gdev.subscruber.ConsoleSubscriber;
import by.gdev.ui.StarterStatusFrame;
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
		log.info("starter was run");
		log.info("starter created {}", DesktopUtil.getTime(Main.class));
		System.setProperty("java.net.preferIPv4Stack", String.valueOf(flag));
		EventBus eventBus = new EventBus();
		StarterAppConfig starterConfig = StarterAppConfig.DEFAULT_CONFIG;
		JCommander.newBuilder().addObject(starterConfig).build().parse(args);
		loadLogbackConfig(starterConfig);
		// fix for new and old client without /
		starterConfig.setServerFile(starterConfig.getServerFile().stream().map(e -> {
			if (!e.endsWith("/")) {
				log.warn("does't end with /, will add, {}", e);
				return e + "/";
			} else
				return e;
		}).collect(Collectors.toList()));
		ResourceBundle bundle = null;

		try {
			client = HttpClientConfig.getInstanceHttpClient();
			bundle = ResourceBundle.getBundle("application", new Localise().getLocal());
			StarterStatusFrame starterStatusFrame = null;
			if (!GraphicsEnvironment.isHeadless()) {
				starterStatusFrame = new StarterStatusFrame("get installed app name", true,
						ResourceBundle.getBundle("application", new Localise().getLocal()));
				starterStatusFrame.setVisible(true);
				eventBus.register(starterStatusFrame);
				eventBus.register(new ViewSubscriber(starterStatusFrame, bundle, OSInfo.getOSType(), starterConfig));
			}
			if (starterConfig.isProd() && !starterConfig.getServerFile().equals(StarterAppConfig.URI_APP_CONFIG)) {
				String errorMessage = String.format(
						"The prod parameter is true. You don't need to change the value of the field. Current: %s, should be: %s",
						starterConfig.getServerFile(), StarterAppConfig.URI_APP_CONFIG);
				throw new RuntimeException(errorMessage);
			}
			Starter s = new Starter(eventBus, starterConfig, bundle, starterStatusFrame);
			eventBus.register(new ConsoleSubscriber(bundle, s.getFileMapperService(), starterConfig));
			s.updateApplication();
			s.validateEnvironmentAndAppRequirements();
			s.prepareResources();
			s.runApp();
			UpdateCore.deleteTmpFileIfExist();

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
			} else if (Objects.nonNull(message) && message.contains("GetIpAddrTable"))
				eventBus.post(new ExceptionMessage(bundle.getString("get.ip.addr.table")));
			else {
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
}