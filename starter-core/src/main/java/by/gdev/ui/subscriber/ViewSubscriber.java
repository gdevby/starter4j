package by.gdev.ui.subscriber;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.google.common.eventbus.Subscribe;

import by.gdev.http.upload.exeption.HashSumAndSizeError;
import by.gdev.http.upload.exeption.UploadFileException;
import by.gdev.http.upload.model.downloader.DownloaderStatus;
import by.gdev.http.upload.model.downloader.DownloaderStatusEnum;
import by.gdev.model.ExceptionMessage;
import by.gdev.model.StatusModel;
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

	@Subscribe
	private void procces(StatusModel status) {
		if (Objects.nonNull(status.getErrorCode())) {
			if (status.getErrorCode() == -1073740791) {
				message(new ExceptionMessage(bundle.getString("driver.error"),"https://gdev.by/help/java/closed-minecraft-1073740791.html"));
			}
			else if (status.getErrorCode() == -1073740771)
				message(new ExceptionMessage(bundle.getString("msi.afterburner.error")));
			else if (status.getErrorCode() != 0)
				message(new ExceptionMessage(bundle.getString("unidentified.error")));
		}
	}

	@Subscribe
	public void message(DownloaderStatus status) {
		if (DownloaderStatusEnum.DONE.equals(status.getDownloaderStatusEnum())) {
			if (!status.getThrowables().isEmpty()) {
				Throwable t = status.getThrowables().get(0);
				if (t instanceof UploadFileException) {
					UploadFileException t1 = (UploadFileException) t;
					String s = String.format(bundle.getString("upload.error"), t1.getUri(), t1.getLocalPath(),
							t1.getLocalizedMessage());
					message(new ExceptionMessage(s));
				} else if (t instanceof HashSumAndSizeError) {
					HashSumAndSizeError t1 = (HashSumAndSizeError) t;
					String s = String.format(bundle.getString("upload.error.hash.sum"), t1.getUri());
					message(new ExceptionMessage(s));
				}
			}
		}
	}

	@Subscribe
	public void message(ExceptionMessage s) {
		JLabelHtmlWrapper label = new JLabelHtmlWrapper(s.getMessage());
		if(Objects.nonNull(s.getLink())) {
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
