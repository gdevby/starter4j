package by.gdev.generator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;

import org.junit.Test;
import org.junit.Assert;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import by.gdev.generator.model.AppConfigModel;
import by.gdev.generator.service.FileMapperService;

public class FileMapperTest {
	@Test
	public void test() throws FileNotFoundException, IOException, NoSuchAlgorithmException {
		Path p = Paths.get("src/test/resources/test-copy");
		FileMapperService f = new FileMapperService(new Gson(), StandardCharsets.UTF_8);
		AppConfigModel configFile  = new AppConfigModel();
		configFile.setAppFolder("src/test/starter-app-folder");
		f.copyFile(Paths.get(configFile.getAppFolder()), p);

		Assert.assertEquals(p.toFile().exists(), true);
		
		

//		Assert.assertNull(f);

		
	}
}
