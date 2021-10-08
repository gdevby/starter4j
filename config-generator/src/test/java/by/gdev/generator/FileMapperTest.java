package by.gdev.generator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import by.gdev.generator.model.AppConfigModel;

public class FileMapperTest {
	
	@Before
	public void deleteFolder() throws IOException {
		File f = Paths.get("target/test").toFile();
		FileUtils.deleteDirectory(f);
	}

	
	@Test
	public void test() throws FileNotFoundException, IOException, NoSuchAlgorithmException {
		Path p = Paths.get("target/test");
		AppConfigModel configFile  = new AppConfigModel();
		configFile.setAppFolder("src/test/starter-app-folder");
		Assert.assertEquals(p.toFile().exists(), true);	
	}
}
