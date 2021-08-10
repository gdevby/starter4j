package by.gdev.generator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.google.gson.Gson;

import by.gdev.generator.AppConfigCreator;
import by.gdev.generator.Domain;
import by.gdev.generator.model.AppConfigModel;
import by.gdev.generator.model.JVMConfig;
import by.gdev.generator.service.FileMapperService;
import by.gdev.util.OSInfo;
import by.gdev.util.OSInfo.Arch;
import by.gdev.util.OSInfo.OSType;
import by.gdev.util.model.download.Repo;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@RunWith(JUnit4.class)
public class AppConfigCreatorTest {
	@BeforeClass
//	@Before
	public void init() {
		log.info("test");
	}
	@Test
	public void test1() throws NoSuchAlgorithmException, IOException {
//		fill with proper data
		JVMConfig jvmProper = new JVMConfig();
		Map<OSType, Map<Arch, Map<String,Repo>>> jvms = new HashMap<OSInfo.OSType, Map<Arch,Map<String,Repo>>>();
		jvms.put(OSType.LINUX,new HashMap<OSInfo.Arch, Map<String,Repo>>());
		jvmProper.setJvms(jvms);

		List<Domain> domains = new ArrayList<Domain>();
		Domain d1 = new Domain();
		d1.setDomain("https://test.com/");
		AppConfigModel configFile  = new AppConfigModel();
		configFile.setJavaFolder("src/test/resources/jvms2");
		FileMapperService f = new FileMapperService(new Gson(), StandardCharsets.UTF_8);
		AppConfigCreator a = new AppConfigCreator(f);
		a.createJreConfig(domains, configFile);
		JVMConfig jvm = new JVMConfig();
		Assert.assertEquals(jvmProper,jvm  );

	}
	@Test
	public void t1() {

	}
}
