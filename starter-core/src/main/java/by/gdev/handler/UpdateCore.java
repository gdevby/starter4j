package by.gdev.handler;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import com.google.common.reflect.TypeToken;

import by.gdev.http.download.service.GsonService;
import by.gdev.model.StarterUpdate;
import by.gdev.ui.JLabelHtmlWrapper;
import by.gdev.util.DesktopUtil;
import by.gdev.util.OSInfo.OSType;
import ch.qos.logback.core.util.FileUtil;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class UpdateCore {

	private ResourceBundle bundle;
	private GsonService gsonService;
	private CloseableHttpClient httpclient;
	private RequestConfig requestConfig;

	public void checkUpdates(OSType osType, String updateConfigUri) throws IOException, NoSuchAlgorithmException {
		Type mapType = new TypeToken<Map<OSType, StarterUpdate>>() {
			private static final long serialVersionUID = 1L;
		}.getType();
		Map<OSType, StarterUpdate> map = gsonService.getObjectWithoutSaving(updateConfigUri, mapType);
		if (!map.containsKey(osType))
			return;
		StarterUpdate update = map.get(osType);
		File jarFile = new File(URLDecoder
				.decode(FileUtil.class.getProtectionDomain().getCodeSource().getLocation().getPath(), "UTF-8"));
		String localeSha1 = DesktopUtil.getChecksum(jarFile, "SHA-1");
		File temp = new File(jarFile.toString() + ".temp");
		if (!update.getSha1().equals(localeSha1)) {
			JLabelHtmlWrapper label = new JLabelHtmlWrapper(bundle.getString("update.message"));
			JOptionPane.showMessageDialog(new JFrame(), label);
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
			Files.move(temp.toPath(), jarFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			System.exit(0);
		}
	}
}