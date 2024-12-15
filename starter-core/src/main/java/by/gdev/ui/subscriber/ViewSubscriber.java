package by.gdev.ui.subscriber;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.zip.GZIPOutputStream;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.util.EntityUtils;

import com.google.common.eventbus.Subscribe;

import by.gdev.Main;
import by.gdev.http.download.exeption.HashSumAndSizeError;
import by.gdev.http.download.exeption.UploadFileException;
import by.gdev.http.upload.download.downloader.DownloaderStatus;
import by.gdev.http.upload.download.downloader.DownloaderStatusEnum;
import by.gdev.model.ExceptionMessage;
import by.gdev.model.LogResponse;
import by.gdev.model.StarterAppConfig;
import by.gdev.model.StarterAppProcess;
import by.gdev.ui.StarterStatusFrame;
import by.gdev.util.CoreUtil;
import by.gdev.util.DesktopUtil;
import by.gdev.util.OSInfo.OSType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
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
				String s1 = Objects.nonNull(starterConfig.getLogURIService()) ? "unidentified.error"
						: "unidentified.error.1";
				ExceptionMessage e = new ExceptionMessage(bundle.getString(s1));
				e.setLogButton(true);
				message(e);
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
					message(new ExceptionMessage(bundle.getString("net.problem")));
				}
			}
		}
	}

	@Subscribe
	public void message(ExceptionMessage s) {
		JTextPane f = getTextPaneWithMessage(s);

		JPanel p = new JPanel();
		BoxLayout bl = new BoxLayout(p, BoxLayout.Y_AXIS);
		p.setLayout(bl);
		p.add(f, BorderLayout.CENTER);
		if (s.isLogButton() && Objects.nonNull(starterConfig.getLogURIService()))
			addLogOffer(p);
		if (Objects.nonNull(s.getLink())) {
			f.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (SwingUtilities.isLeftMouseButton(e)) {
						DesktopUtil.openLink(osType, s.getLink());
					}
				}
			});
		}
		JOptionPane.showMessageDialog(frame, p, "", JOptionPane.ERROR_MESSAGE);

	}

	protected void addLogOffer(JPanel p) {
		JPanel p1 = new JPanel();
		JButton b = new JButton(bundle.getString("link.get"));
		JLabel l = new JLabel(bundle.getString("preparing"));
		JTextPane tp = getTextPaneWithMessage(new ExceptionMessage(""));
		p1.add(b);
		p1.add(l);
		p1.add(tp);
		tp.setVisible(false);
		l.setVisible(false);
		b.addActionListener(e -> {
			b.setVisible(false);
			l.setVisible(true);
			doRequest(p1, l, tp);
		});
		tp.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		p.add(p1, BorderLayout.SOUTH);

	}

	private void doRequest(JPanel p, JLabel l, JTextPane tp) {
		CompletableFuture.runAsync(() -> {
			HttpPost method = new HttpPost(starterConfig.getLogURIService());
			CloseableHttpResponse response = null;
			Pair<String, byte[]> pair = null;
			try {
				pair = CoreUtil.readFileLog();
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				GZIPOutputStream g = new GZIPOutputStream(out);
				g.write(pair.getValue());
				g.close();
				byte[] body = out.toByteArray();
				method.setEntity(new ByteArrayEntity(body));
				method.setConfig(RequestConfig.custom().setConnectTimeout(60000).setSocketTimeout(60000).build());
				response = Main.client.execute(method);
				if (response.getStatusLine().getStatusCode() >= 300) {
					log.info("not proper code " + response.getStatusLine().toString());
					showError(p, pair);
				} else {
					LogResponse lr = Main.GSON.fromJson(
							IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8),
							LogResponse.class);
					SwingUtilities.invokeLater(() -> {
						tp.setText(String.format("<html>%s</html>", lr.getLink()));
						StringSelection selection = new StringSelection(lr.getLink());
						Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
						clipboard.setContents(selection, selection);
						l.setVisible(false);
						tp.setVisible(true);
						JOptionPane.showMessageDialog(frame, bundle.getString("clipboard.copy"), "",
								JOptionPane.INFORMATION_MESSAGE);
					});
				}
			} catch (Exception e1) {
				log.error("exception", e1);
				showError(p, pair);
			} finally {
				if (Objects.nonNull(response)) {
					method.abort();
					EntityUtils.consumeQuietly(response.getEntity());
				}
			}
		});
	}

	private void showError(JPanel p, Pair<String, byte[]> pair) {
		JTextPane tp = getTextPaneWithMessage(new ExceptionMessage(
				String.format(bundle.getString("error.log.send"), Objects.isNull(pair) ? "" : pair.getKey()), ""));
		p.setVisible(false);
		JOptionPane.showMessageDialog(frame, tp, null, JOptionPane.ERROR_MESSAGE);
	}

	protected JTextPane getTextPaneWithMessage(ExceptionMessage s) {
		JTextPane f = new JTextPane();
		f.setContentType("text/html");
		f.setText(String.format("<html>%s</html>",
				s.getMessage() + (Objects.nonNull(s.getError())
						? "<br> <br>" + ExceptionUtils.getStackTrace(s.getError()).replaceAll("\n", "<br>")
						: "")));
		f.setEditable(false);
		f.setBackground(null);
		f.setBorder(null);
		return f;
	}
}
