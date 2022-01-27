package by.gdev;

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
			
			Starter s = new Starter(eventBus, starterConfig, bundle);
			s.collectOSInfoAndRegisterSubscriber();
			s.validateEnvironmentAndAppRequirements();
			s.prepareResources();
			s.runApp();
		} catch (FileSystemException ex) {
			log.error("error", ex);
			if (Objects.nonNull(bundle)) {
				eventBus.post(new ExceptionMessage(String.format(bundle.getString("file.delete.problem"),ex.getLocalizedMessage()),
						"https://gdev.by/help/java/check-disk.html"));
			}
		} catch (Throwable t) {
			log.error("Error", t);
			System.exit(-1);
		}
	}
}