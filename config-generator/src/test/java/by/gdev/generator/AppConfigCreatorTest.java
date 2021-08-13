package by.gdev.generator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import by.gdev.generator.model.AppConfigModel;
import by.gdev.generator.model.JVMConfig;
import by.gdev.util.OSInfo;
import by.gdev.util.OSInfo.Arch;
import by.gdev.util.OSInfo.OSType;
import by.gdev.util.model.download.Repo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AppConfigCreatorTest {

	@Test
	public void test1() throws NoSuchAlgorithmException, IOException {
		JVMConfig jvmProper = new JVMConfig();
		Map<OSType, Map<Arch, Map<String, Repo>>> jvms = new HashMap<OSInfo.OSType, Map<Arch, Map<String, Repo>>>();
		Map<Arch, Map<String, Repo>> arch = new HashMap<OSInfo.Arch, Map<String, Repo>>();
		arch.put(Arch.x64, new HashMap<String, Repo>());
		jvms.put(OSType.LINUX, arch);
		jvmProper.setJvms(jvms);
		System.out.println(jvmProper);
		AppConfigModel configFile = new AppConfigModel();
		configFile.setJavaFolder("src/test/resources/jvms2");
		JVMConfig jvm = new JVMConfig();
		jvm.setJvms(new HashMap<OSInfo.OSType, Map<Arch, Map<String, Repo>>>());
		Path path = Paths.get(configFile.getJavaFolder());
		for (Path pO : Files.walk(path, 1).filter(entry -> !entry.equals(path)).collect(Collectors.toList())) {
			OSType t = OSType.valueOf(pO.getFileName().toString().toUpperCase(Locale.ROOT));
			jvm.getJvms().put(t, new HashMap<OSInfo.Arch, Map<String, Repo>>());
			for (Path pA : Files.walk(pO, 1).filter(entry -> !entry.equals(pO)).collect(Collectors.toList())) {
				Arch a = Arch.valueOf(pA.getFileName().toString().toLowerCase(Locale.ROOT));
				jvm.getJvms().get(t).put(a, new HashMap<String, Repo>());
				System.out.println(jvm);
			}
			Assert.assertEquals(jvmProper, jvm);
		}
	}
}