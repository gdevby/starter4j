package by.gdev.utils.service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.gson.Gson;

import by.gdev.util.excepiton.NotAllowWriteFileOperation;
import lombok.extern.slf4j.Slf4j;
/**
 * This class is intended for reading json to get object and writing json files to working directory.
 */
@Slf4j
public class FileMapperService {
	private Gson gson;
	private Charset charset;
	private String workingDirectory;

	public FileMapperService(Gson gson, Charset charset, String workingDirectory) {
		this.gson = gson;
		this.charset = charset;
		this.workingDirectory = workingDirectory;
	}
	/**
	 * 
	 * @param create
	 * @param config
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws UnsupportedOperationException can't write to file , because file has only read operation 
	 */
	public void write(Object create, String config) throws IOException, UnsupportedOperationException {
		Path path = Paths.get(workingDirectory, config);
		if (Files.notExists(path.getParent()))
			Files.createDirectories(path.getParent());
		if(Files.exists(path) && !path.toFile().canWrite()) 
			throw new NotAllowWriteFileOperation(path.toString());
		try (OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(path.toFile()), charset)) {
			gson.toJson(create, out);
		}
	}
	
	public <T> T read(String file, Class<T> cl) {
		try (InputStreamReader read = new InputStreamReader(new FileInputStream(Paths.get(workingDirectory, file).toFile()),charset)) {
			return gson.fromJson(read, cl);
		}catch(FileNotFoundException e) {
			log.info("file not exist "+ file);
		}catch(Throwable t) {
			log.warn("error read json "+ file, t);
		}
		return null;
	}
}