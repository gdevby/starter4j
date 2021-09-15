package by.gdev.generator;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import com.beust.jcommander.JCommander;

import by.gdev.generator.model.AppConfigModel;

public class AppConfigModelTest {
	@Test
	public void test() {
		AppConfigModel acm = new AppConfigModel();
		String[] argv = { "-name", "new-name", "-version", "1.1", "-jvm", "first", "-jvm","second", "-flag"};
		JCommander.newBuilder().addObject(acm).build().parse(argv);
		Assert.assertEquals(acm.getAppName(), "new-name");
		Assert.assertEquals(acm.getAppVersion(), 1.1, 0.0);
		Assert.assertEquals(acm.getJvmArguments(), Arrays.asList("first", "second"));
		Assert.assertTrue(acm.isGeneretedJava());
	}
}
