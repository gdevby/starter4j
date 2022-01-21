package by.gdev.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import com.google.common.eventbus.Subscribe;

import by.gdev.http.upload.model.downloader.DownloaderStatus;
import by.gdev.http.upload.model.downloader.DownloaderStatusEnum;
import by.gdev.util.DesktopUtil;
import by.gdev.util.OSInfo.OSType;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StarterStatusFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	private JProgressBar progressBar;
	private String gdevBy = "https://github.com/gdevby/desktop-starter-launch-update-bootstrap";
	private JLabel uploadStatus = new JLabel();
	private ResourceBundle resourceBundle;

	public StarterStatusFrame(OSType type, String appName, boolean indeterminate, ResourceBundle resourceBundle) {
		this.resourceBundle = resourceBundle;
		setResizable(false);
		setUndecorated(true);
		setAlwaysOnTop(true);
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		int width = gd.getDisplayMode().getWidth();
		int height = gd.getDisplayMode().getHeight();
		setSize(new Dimension(width / 5, height / 6));
		DesktopUtil.initLookAndFeel();

		JPanel p = new JPanel(new BorderLayout(0, 0));
		BufferedImage image = getImage();
		JPanel background = new JPanel(new BorderLayout(0, 0)) {
			private static final long serialVersionUID = 1L;

			@Override
			public void paint(Graphics g) {
				if (Objects.nonNull(image))
					g.drawImage(image, 0, 0, null);
				super.paint(g);
			}
		};

		background.setOpaque(false);
		JLabel label = new JLabel("app launcher gdev.by");
		label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		Font f = label.getFont();
		label.setFont(f.deriveFont(Font.BOLD).deriveFont((float) (f.getSize() - 2)));
		p.setOpaque(true);
		JLabel nameLabel = new JLabel(appName);
		f = nameLabel.getFont();
		nameLabel.setFont(f.deriveFont((float) (f.getSize() + 5)));
		nameLabel.setHorizontalAlignment(JLabel.CENTER);

		uploadStatus.setFont(uploadStatus.getFont().deriveFont(Font.BOLD));
		uploadStatus.setHorizontalAlignment(JLabel.RIGHT);
		uploadStatus.setBorder(BorderFactory.createEmptyBorder(0, 0, 3, 3));

		progressBar = new JProgressBar();
		progressBar.setDoubleBuffered(true);
		progressBar.setIndeterminate(indeterminate);
		progressBar.setBorder(BorderFactory.createEmptyBorder());

		background.add(nameLabel, BorderLayout.CENTER);
		background.add(label, BorderLayout.NORTH);
		background.add(uploadStatus, BorderLayout.SOUTH);

		p.add(background, BorderLayout.CENTER);
		p.add(progressBar, BorderLayout.SOUTH);

		add(p);
		this.setLocation(width / 2 - this.getSize().width / 2, height / 2 - this.getSize().height / 2);

		label.addMouseListener(new MouseAdapter() {
			private Color c = label.getForeground();

			@Override
			public void mouseClicked(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e)) {
					DesktopUtil.openLink(type, gdevBy);
				}
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				label.setForeground(Color.BLACK);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				label.setForeground(c);
			}
		});
	}

	private BufferedImage getImage() {
		BufferedImage image = null;
		try {
			image = ImageIO.read(StarterStatusFrame.class.getResourceAsStream("/background.jpg"));
		} catch (IOException e) {
			log.warn("can't load image", e);
		}
		return image;
	}

	@Subscribe
	public void messageToSpeed(DownloaderStatus status) {
		if (progressBar.isIndeterminate() && DownloaderStatusEnum.WORK.equals(status.getDownloaderStatusEnum())) {
			SwingUtilities.invokeLater(() -> {
				progressBar.setIndeterminate(false);
				updateUploadProgressBar(status);
			});
		} else if (!progressBar.isIndeterminate()) {
			SwingUtilities.invokeLater(() -> {
				updateUploadProgressBar(status);
			});
		}
	}

	private void updateUploadProgressBar(DownloaderStatus status) {
		int uploaded = (int) status.getDownloadSize() / (1024 * 1024);
		int allUpload = (int) status.getAllDownloadSize() / (1024 * 1024);
		uploadStatus.setText(String.format("%s %s/%s %s ", resourceBundle.getString("uploading"), uploaded, allUpload,
				resourceBundle.getString("mb")));
		progressBar.setMaximum(allUpload);
		progressBar.setValue(uploaded);
	}

}
