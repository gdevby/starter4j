package by.gdev;

import java.awt.GraphicsEnvironment;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystemException;
import java.util.Objects;
import java.util.ResourceBundle;

import com.beust.jcommander.JCommander;
import com.google.common.eventbus.EventBus;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import by.gdev.component.Starter;
import by.gdev.handler.Localise;
import by.gdev.model.ExceptionMessage;
import by.gdev.model.StarterAppConfig;
import by.gdev.subscruber.ConsoleSubscriber;
import by.gdev.ui.StarterStatusFrame;
import by.gdev.ui.subscriber.ViewSubscriber;
import by.gdev.util.OSInfo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {
	public static Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	public static Charset charset = StandardCharsets.UTF_8;

	public static void main(String[] args) throws Exception {
		boolean flag = true;
		System.setProperty("java.net.preferIPv4Stack", String.valueOf(flag));
		EventBus eventBus = new EventBus();
		StarterAppConfig starterConfig = StarterAppConfig.DEFAULT_CONFIG;
		JCommander.newBuilder().addObject(starterConfig).build().parse(args);
		ResourceBundle bundle = null;
		try {
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
		} catch (FileSystemException ex) {
			log.error("error", ex);
			if (Objects.nonNull(bundle)) {
				eventBus.post(new ExceptionMessage(
						String.format(bundle.getString("file.delete.problem"), ex.getLocalizedMessage()),
						"https://gdev.by/help/java/check-disk"));
			}
		} catch (Throwable t) {
			String message = t.getMessage();
			if ("file doesn't exist".equals(message)) {
				eventBus.post(new ExceptionMessage(bundle.getString("download.error")));
			} else if (Objects.nonNull(message) && message.contains("GetIpAddrTable"))
				eventBus.post(new ExceptionMessage(bundle.getString("get.ip.addr.table")));
			else {
				eventBus.post(new ExceptionMessage(bundle.getString("unidentified.error")));
			}
			System.exit(-1);
		}
	}
}