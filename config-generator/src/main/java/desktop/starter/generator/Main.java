package desktop.starter.generator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import desktop.starter.generator.model.AppConfigModel;
import desktop.starter.generator.service.FileMapperService;
import desktop.starter.model.AppConfig;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import static desktop.starter.generator.AppConfigCreator.APP_CONFIG_GENERATOR;
import static desktop.starter.generator.AppConfigCreator.DOMAIN_CONFIG;
import static desktop.starter.generator.AppConfigCreator.TEMP_APP_CONFIG;
import static desktop.starter.generator.AppConfigCreator.TARGET_OUT_FOLDER;

@Slf4j
public class Main {
	public static Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	public static Charset charset = StandardCharsets.UTF_8;
	public static FileMapperService fms = new FileMapperService();
	
	public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
		// todo add special library to process args
		int length = args.length;
		String file;
		if (length == 1)
			file = args[0];
		else if (Files.exists(Paths.get(APP_CONFIG_GENERATOR))) {
			file = new File(APP_CONFIG_GENERATOR).getCanonicalPath();
		} else {
			throw new FileNotFoundException(String.format("can't find file %s in directory %s", APP_CONFIG_GENERATOR,
					new File(".").getCanonicalPath()));
		}
		String ftpFile;
		if (length == 2) {
			ftpFile = args[1];
		} else if (Files.exists(Paths.get(DOMAIN_CONFIG))) {
			ftpFile = new File(DOMAIN_CONFIG).getCanonicalPath();
		} else {
			throw new FileNotFoundException(String.format("can't find file %s in directory %s", DOMAIN_CONFIG,
					new File(".").getCanonicalPath()));
		}
		log.info("used FTP_CONFIG {}", ftpFile);
		log.info("used APP_CONFIG_GENERATOR {}", file);
		AppConfigModel c = GSON.fromJson(new InputStreamReader(new FileInputStream(file), charset),
				AppConfigModel.class);
		List<Domain> ftps = GSON.fromJson(new InputStreamReader(new FileInputStream(ftpFile), charset),
				new TypeToken<List<Domain>>() {
				}.getType());
		AppConfigCreator appConfigCreator = new AppConfigCreator();
		// create		
		AppConfig appConfig = appConfigCreator.createConfig(c, ftps);
		log.info("save config before uploading {}", new File(TEMP_APP_CONFIG).getAbsolutePath());	
		fms.write(appConfig, Paths.get(TARGET_OUT_FOLDER, TEMP_APP_CONFIG));
		log.info("upload remote servers");
		log.info("save");
		log.info("DONE");
	}
}
