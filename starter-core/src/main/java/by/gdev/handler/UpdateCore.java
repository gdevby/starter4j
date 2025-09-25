package by.gdev.handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.ResourceBundle;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.commons.io.IOUtils;

import by.gdev.Main;
import by.gdev.http.download.exeption.HashSumAndSizeError;
import by.gdev.http.download.service.FileCacheService;
import by.gdev.http.download.service.GsonService;
import by.gdev.model.StarterAppConfig;
import by.gdev.model.UpdateApp;
import by.gdev.ui.JLabelHtmlWrapper;
import by.gdev.util.DesktopUtil;
import by.gdev.util.OSInfo.OSType;
import by.gdev.util.model.InternetServerMap;
import by.gdev.util.model.download.Metadata;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
public class UpdateCore {

	private ResourceBundle bundle;
	private GsonService gsonService;
	private FileCacheService fileCacheService;
	private StarterAppConfig starterConfig;
	private InternetServerMap domainAvailability;

	public void checkUpdates(OSType osType) throws IOException, NoSuchAlgorithmException {
		UpdateApp ua = getUpdateFile();
		if (ua == null || ua.getMap() == null || !ua.getMap().containsKey(osType))
			return;
		Metadata m = ua.getMap().get(osType);
		File jarFile = new File(
				URLDecoder.decode(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath(), "UTF-8"));
		if (jarFile.toString().endsWith("classes"))
			return;
		String localeSha1 = DesktopUtil.getChecksum(jarFile, "SHA-1");
		ProcessBuilder pb = null;
		if (!m.getSha1().equals(localeSha1)) {
			pb = DesktopUtil.preparedRestart(jarFile.getAbsolutePath(), Paths.get("").toAbsolutePath().toFile(), null,
					null);
			Path temp = fileCacheService.getRawObject(ua.getUrls(), m, false);
			JLabelHtmlWrapper label = new JLabelHtmlWrapper(bundle.getString("update.message"));
			JOptionPane.showMessageDialog(new JFrame(), label, "", JOptionPane.INFORMATION_MESSAGE);
			String hash = DesktopUtil.getChecksum(temp.toFile(), "SHA-1");
			if (!hash.equals(m.getSha1())) {
				throw new HashSumAndSizeError(ua.getUrls().toString(), m.toString() + " " + hash, "");
			}
			log.info("from {} to {}", temp.toString(), jarFile.toPath().toString());
			try (OutputStream outputStream = new FileOutputStream(jarFile)) {
				IOUtils.copy(new FileInputStream(temp.toFile()), outputStream);
			}
			pb.start();
			System.exit(0);
		}
	}

	private UpdateApp getUpdateFile() throws IOException {
		if (domainAvailability.hasInternetForDomains(starterConfig.getServerFile())) {
			return gsonService.getObjectWithoutSaving(starterConfig.getServerFile(),
					StarterAppConfig.APP_STARTER_UPDATE_CONFIG, UpdateApp.class);
		}
		return null;
	}
}