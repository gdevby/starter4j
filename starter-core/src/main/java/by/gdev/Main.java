package by.gdev;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.beust.jcommander.JCommander;
import com.google.common.eventbus.EventBus;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import by.gdev.component.Starter;
import by.gdev.model.StarterAppConfig;
import by.gdev.subscruber.ConsoleSubscriber;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {
	public static Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	public static Charset charset = StandardCharsets.UTF_8;

	public static void main(String[] args) throws Exception {
		boolean flag = true;
		//TODO Can get encoding from args and it is too java.net.preferIPv4Stack
		System.setProperty("java.net.preferIPv4Stack", String.valueOf(flag));
		EventBus eventBus = new EventBus();
		eventBus.register(new ConsoleSubscriber());
		StarterAppConfig starterConfig = StarterAppConfig.DEFAULT_CONFIG;
		JCommander.newBuilder().addObject(starterConfig).build().parse(args);
		try {
			Starter s = new Starter(eventBus, starterConfig);
			s.collectOSInfo();
			s.validateEnvironmentAndAppRequirements();
			s.prepareResources();
			s.runApp();
		} catch (Throwable t) {
			log.error("Error", t);
			System.exit(-1);
		}
	}
}