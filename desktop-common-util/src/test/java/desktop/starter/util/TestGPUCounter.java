package desktop.starter.util;

import desktop.starter.util.os.WindowsExecutor;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;


public class TestGPUCounter {
    @Test
    public void test() throws IOException {
        String s = IOUtils.toString(TestGPUCounter.class.getResourceAsStream("2_gpus.txt"),
                StandardCharsets.UTF_8);
        WindowsExecutor os = new WindowsExecutor();
        Assert.assertEquals(os.processSystemInfoLines(Arrays.asList(s.split("\r\n"))).getGpus().size(), 2);

        s = IOUtils.toString(TestGPUCounter.class.getResourceAsStream("5_gpus.txt"),
                StandardCharsets.UTF_8);
        Assert.assertEquals(os.processSystemInfoLines(Arrays.asList(s.split("\r\n"))).getGpus().size(), 5);
    }
}
