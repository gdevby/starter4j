package by.gdev.ui.subscriber;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.google.common.eventbus.Subscribe;

import by.gdev.http.download.exeption.HashSumAndSizeError;
import by.gdev.http.download.exeption.UploadFileException;
import by.gdev.http.upload.download.downloader.DownloaderStatus;
import by.gdev.http.upload.download.downloader.DownloaderStatusEnum;
import by.gdev.model.ExceptionMessage;
import by.gdev.model.StarterAppConfig;
import by.gdev.model.StarterAppProcess;
import by.gdev.ui.JLabelHtmlWrapper;
import by.gdev.ui.StarterStatusFrame;
import by.gdev.util.DesktopUtil;
import by.gdev.util.OSInfo.OSType;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ViewSubscriber {

	private StarterStatusFrame frame;
	private ResourceBundle bundle;
	private OSType osType;
	private StarterAppConfig starterConfig;

	@Subscribe
	private void procces(StarterAppProcess status) {
		if (!StringUtils.isEmpty(status.getLine())
				&& status.getLine().equals("java.lang.UnsatisfiedLinkError: no zip in java.library.path")) {
			message(new ExceptionMessage(String.format(bundle.getString("unsatisfied.link.error"),
					Paths.get(starterConfig.getWorkDirectory()).toAbsolutePath().toString(),
					"C:\\" + starterConfig.getWorkDirectory())));
		}
		if (Objects.nonNull(status.getErrorCode())) {
			if (status.getErrorCode() == -1073740791) {
				message(new ExceptionMessage(bundle.getString("driver.error"),
						"https://gdev.by/help/java/closed-1073740791"));
			} else if (status.getErrorCode() == -1073740771)
				message(new ExceptionMessage(bundle.getString("msi.afterburner.error")));
			else if (status.getErrorCode() != 0) {
				message(new ExceptionMessage(bundle.getString("unidentified.error")));
				System.exit(0);
			}
		}
	}

	@Subscribe
	public void message(DownloaderStatus status) {
		if (DownloaderStatusEnum.DONE.equals(status.getDownloaderStatusEnum())) {
			if (!status.getThrowables().isEmpty()) {
				Throwable t = status.getThrowables().get(0);
				if (t instanceof HashSumAndSizeError) {
					HashSumAndSizeError t1 = (HashSumAndSizeError) t;
					String s = String.format(bundle.getString("upload.error.hash.sum"), t1.getUri(), t1.getMessage());
					message(new ExceptionMessage(s, t1.getUri()));
				} else if (t instanceof UploadFileException) {
					UploadFileException t1 = (UploadFileException) t;
					String s = String.format(bundle.getString("upload.error"), t1.getUri(), t1.getMessage());
					message(new ExceptionMessage(s));
				}
			}
		}
	}

	@Subscribe
	public void message(ExceptionMessage s) {
		JLabelHtmlWrapper label = new JLabelHtmlWrapper(s.getMessage() + (Objects.nonNull(s.getError())
				? "<br> <br>" + ExceptionUtils.getStackTrace(s.getError()).replaceAll("\n", "<br>")
				: ""));
		if (Objects.nonNull(s.getLink())) {
			label.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (SwingUtilities.isLeftMouseButton(e)) {
						DesktopUtil.openLink(osType, s.getLink());
					}
				}
			});
		}
		JOptionPane.showMessageDialog(frame, label, "", JOptionPane.ERROR_MESSAGE);

	}
}
