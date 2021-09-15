package by.gdev.generator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;

import org.junit.Test;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;

import com.google.gson.Gson;

import by.gdev.generator.model.AppConfigModel;
import by.gdev.generator.service.FileMapperService;

public class FileMapperTest {
	
	@Before
	public void deleteFolder() throws IOException {
		File f = Paths.get("target/test").toFile();
		FileUtils.deleteDirectory(f);
	}

	
	@Test
	public void test() throws FileNotFoundException, IOException, NoSuchAlgorithmException {
		Path p = Paths.get("target/test");
		FileMapperService f = new FileMapperService(new Gson(), StandardCharsets.UTF_8);
		AppConfigModel configFile  = new AppConfigModel();
		configFile.setAppFolder("src/test/starter-app-folder");
		f.copyFile(Paths.get(configFile.getAppFolder()), p);
		Assert.assertEquals(p.toFile().exists(), true);	
	}
}
