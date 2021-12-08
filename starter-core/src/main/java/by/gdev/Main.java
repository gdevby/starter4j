package by.gdev;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.beust.jcommander.JCommander;
import com.google.common.eventbus.EventBus;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import by.gdev.component.Bootstrapper;
import by.gdev.model.StarterAppConfig;
import by.gdev.subscruber.ConsoleSubscriber;

public class Main {
	public static Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	public static Charset charset = StandardCharsets.UTF_8;

	public static void main(String[] args) throws Exception {
		//тут можно инициализировать нашу созданную ошибку и вывожить в кэтче
		boolean flag = true;
		System.setProperty("java.net.preferIPv4Stack", String.valueOf(flag));
		ConsoleSubscriber listener = new ConsoleSubscriber();
		StarterAppConfig starterConfig = StarterAppConfig.DEFAULT_CONFIG;
		JCommander.newBuilder().addObject(starterConfig).build().parse(args);
		EventBus eventBus = new EventBus();
		eventBus.register(listener);
		
		try {
			Bootstrapper s = new Bootstrapper(eventBus, starterConfig);
			s.collectOSInfo();
			s.validateEnvironmentAndAppRequirements();
			s.prepareResources();
			s.runApp();
		} catch (Throwable t) {
			t.printStackTrace();
			System.exit(-1);
		}
	}
}