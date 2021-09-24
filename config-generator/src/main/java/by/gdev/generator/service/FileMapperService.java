package by.gdev.generator.service;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;




public class FileMapperService {
	Gson GSON;
	Charset charset;
	
	public FileMapperService(Gson GSON, Charset charset) {
		this.GSON = GSON;
		this.charset = charset;
	}
	
	public void write(Object create, Path config) throws FileNotFoundException, IOException {
		if (Files.notExists(config.getParent()))
			Files.createDirectories(config.getParent());
		try (OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(config.toFile()), charset)) {
			GSON.toJson(create, out);
		}
	}

	public Object read(Path file, Class<?> clas) throws FileNotFoundException, IOException {
		try (BufferedReader read = new BufferedReader(new FileReader(file.toFile()))){
			return GSON.fromJson(read, clas);
		}
	}
	public Object readToken(Path file, Type typ) throws FileNotFoundException, IOException {
		try (BufferedReader read = new BufferedReader(new FileReader(file.toFile()))){
			return GSON.fromJson(read, typ);
		}		
	}

	public void copyFile(Path source, Path dest) throws IOException {
	    FileUtils.copyDirectory(source.toFile(), dest.toFile());
	}
}
