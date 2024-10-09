package by.gdev.handler;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import com.google.common.reflect.TypeToken;

import by.gdev.Main;
import by.gdev.http.download.service.GsonService;
import by.gdev.model.StarterAppConfig;
import by.gdev.model.StarterUpdate;
import by.gdev.ui.JLabelHtmlWrapper;
import by.gdev.util.DesktopUtil;
import by.gdev.util.OSInfo.OSType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
public class UpdateCore {

	private ResourceBundle bundle;
	private GsonService gsonService;
	private CloseableHttpClient httpclient;
	private RequestConfig requestConfig;
	private StarterAppConfig starterConfig;

	public void checkUpdates(OSType osType) throws IOException, NoSuchAlgorithmException {
		Map<OSType, StarterUpdate> map = getUpdateFile();
		if (map == null || !map.containsKey(osType))
			return;
		StarterUpdate update = map.get(osType);
		File jarFile = new File(
				URLDecoder.decode(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath(), "UTF-8"));
		if (jarFile.toString().endsWith("classes"))
			return;
		String localeSha1 = DesktopUtil.getChecksum(jarFile, "SHA-1");
		File temp = new File(jarFile.toString() + ".temp");
		if (!update.getSha1().equals(localeSha1)) {
			BufferedInputStream in = null;
			BufferedOutputStream out = null;
			HttpGet httpGet = new HttpGet(update.getUri());
			try {
				httpGet.setConfig(requestConfig);
				CloseableHttpResponse response = httpclient.execute(httpGet);
				HttpEntity entity = response.getEntity();
				in = new BufferedInputStream(entity.getContent());
				out = new BufferedOutputStream(new FileOutputStream(temp));
				byte[] buffer = new byte[1024];
				int curread = in.read(buffer);
				while (curread != -1) {
					out.write(buffer, 0, curread);
					curread = in.read(buffer);
				}
			} finally {
				httpGet.abort();
				IOUtils.close(out);
				IOUtils.close(in);
			}
			JLabelHtmlWrapper label = new JLabelHtmlWrapper(bundle.getString("update.message"));
			JOptionPane.showMessageDialog(new JFrame(), label, "", JOptionPane.INFORMATION_MESSAGE);
			log.info("from {} to {}", temp.toPath().toString(), jarFile.toPath().toString());
			try (OutputStream outputStream = new FileOutputStream(jarFile)) {
				IOUtils.copy(new FileInputStream(temp), outputStream);
			}
			System.exit(0);
		}
	}

	private Map<OSType, StarterUpdate> getUpdateFile() throws IOException {
		return gsonService.getObjectWithoutSaving(starterConfig.getServerFile(),
				StarterAppConfig.APP_STARTER_UPDATE_CONFIG, new TypeToken<Map<OSType, StarterUpdate>>() {
					private static final long serialVersionUID = 1L;
				}.getType());
	}

	public static void deleteTmpFileIfExist() {
		try {
			File jarFile = new File(URLDecoder
					.decode(FileUtils.class.getProtectionDomain().getCodeSource().getLocation().getPath(), "UTF-8"));
			Files.deleteIfExists(new File(jarFile.toString() + ".temp").toPath());
		} catch (Throwable e) {
			log.error("error", e);
		}
	}
}