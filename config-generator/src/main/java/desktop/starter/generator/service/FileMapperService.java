package desktop.starter.generator.service;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class FileMapperService {
	//todo used one gson from main
    static Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    static Charset charset = StandardCharsets.UTF_8;

	public static void write(Object create, Path config) throws FileNotFoundException, IOException {
		if (Files.notExists(config.getParent()))
			Files.createDirectories(config.getParent());
		try (OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(config.toFile()), charset)) {
			GSON.toJson(create, out);
		}
	}

	public static Object read(Path file, Class<?> clas) throws FileNotFoundException, IOException {
		try (BufferedReader read = new BufferedReader(new FileReader(file.toFile())) ){
			return GSON.fromJson(read, clas);
		}
	}

	public static void copyFile(Path source, Path dest) throws IOException {
	    FileUtils.copyDirectory(source.toFile(), dest.toFile());
	}

}
