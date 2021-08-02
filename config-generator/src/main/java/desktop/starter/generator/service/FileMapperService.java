package desktop.starter.generator.service;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;

import desktop.starter.generator.Main;

public class FileMapperService {

	public void write(Object create, Path config) throws FileNotFoundException, IOException {
		if (Files.notExists(config.getParent()))
			Files.createDirectories(config.getParent());
		try (OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(config.toFile()), Main.charset)) {
			Main.GSON.toJson(create, out);
		}
	}

	public Object read(Path file, Class<?> clas) throws FileNotFoundException, IOException {
		try (BufferedReader read = new BufferedReader(new FileReader(file.toFile())) ){
			return Main.GSON.fromJson(read, clas);
		}
	}

	public void copyFile(Path source, Path dest) throws IOException {
	    FileUtils.copyDirectory(source.toFile(), dest.toFile());
	}
}
