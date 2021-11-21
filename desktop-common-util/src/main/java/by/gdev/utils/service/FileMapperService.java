package by.gdev.utils.service;

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
import java.nio.file.Paths;

import com.google.gson.Gson;

public class FileMapperService {
	private Gson gson;
	private Charset charset;
	private String workingDirectory;

	public FileMapperService(Gson gson, Charset charset, String workingDirectory) {
		this.gson = gson;
		this.charset = charset;
		this.workingDirectory = workingDirectory;
	}

	public void write(Object create, String config) throws FileNotFoundException, IOException {
		Path path = Paths.get(workingDirectory, config);
		if (Files.notExists(path.getParent()))
			Files.createDirectories(path.getParent());
		try (OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(path.toFile()), charset)) {
			gson.toJson(create, out);
		}
	}

	public <T> T read(String file, Class<T> cl) throws FileNotFoundException, IOException {
		try (BufferedReader read = new BufferedReader(new FileReader(Paths.get(workingDirectory, file).toFile()))) {
			return gson.fromJson(read, cl);
		}
	}

	public Object readToken(Path file, Type typ) throws FileNotFoundException, IOException {
		try (BufferedReader read = new BufferedReader(new FileReader(file.toFile()))) {
			return gson.fromJson(read, typ);
		}
	}
}