package by.gdev.generator;

import static by.gdev.generator.AppConfigCreator.TARGET_OUT_FOLDER;
import static by.gdev.generator.AppConfigCreator.TEMP_APP_CONFIG;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;

import com.beust.jcommander.JCommander;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import by.gdev.generator.model.AppConfigModel;
import by.gdev.generator.service.FileMapperService;
import by.gdev.model.AppConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {	
	public static Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	public static Charset charset = StandardCharsets.UTF_8;
	public static FileMapperService fileMapperService = new FileMapperService(GSON, charset);
	
	public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
		AppConfigModel acm = AppConfigModel.DEFAULT_APP_CONFIG_MODEL;
		JCommander jc = JCommander.newBuilder().addObject(acm).build();
		jc.parse(args);
		if (acm.help) {
			jc.usage();
		}
		AppConfigCreator appConfigCreator = new AppConfigCreator(fileMapperService);
		// create	
		AppConfig appConfig = appConfigCreator.createConfig(acm);
		log.info("save config before uploading {}", new File(TARGET_OUT_FOLDER,TEMP_APP_CONFIG).getAbsolutePath());	
		fileMapperService.write(appConfig, Paths.get(TARGET_OUT_FOLDER, TEMP_APP_CONFIG));
		log.info("DONE");
	}
}