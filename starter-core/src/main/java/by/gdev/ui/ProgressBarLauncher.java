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

import by.gdev.http.head.cache.model.downloader.DownloaderStatus;
import by.gdev.util.DesktopUtil;
import by.gdev.util.OSInfo.OSType;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProgressBarLauncher extends JFrame {
	private static final long serialVersionUID = 1L;
	private JProgressBar progressBar;
	private String gdevBy = "https://gdev.by";
	private JLabel uploadStatus = new JLabel();
	private int heightProgressBar;
	private ResourceBundle resourceBundle;

	public ProgressBarLauncher(OSType type, String appName, String version, boolean indeterminate,
			ResourceBundle resourceBundle) {
		this.resourceBundle = resourceBundle;
		setResizable(false);
		setUndecorated(true);
		setAlwaysOnTop(true);
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		int width = gd.getDisplayMode().getWidth();
		int height = gd.getDisplayMode().getHeight();
		setSize(new Dimension(width / 5, height / 6));
		Dimension size = getSize();
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
		JLabel label = new JLabel("Java app launcher " + gdevBy);
		label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		Font f = label.getFont();
		label.setFont(f.deriveFont(Font.BOLD).deriveFont((float) (f.getSize() - 2)));
		heightProgressBar = (int) (label.getPreferredSize().getHeight() * 1.5);
		p.setOpaque(true);
		JLabel nameLabel = new JLabel(appName);
		f = nameLabel.getFont();
		nameLabel.setFont(f.deriveFont((float) (f.getSize() + 5)));
		nameLabel.setHorizontalAlignment(JLabel.CENTER);

		JLabel versionLabel = new JLabel(version);
		versionLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 10));
		f = versionLabel.getFont();
		versionLabel.setFont(f.deriveFont((float) (f.getSize() + 3)));
		versionLabel.setHorizontalAlignment(JLabel.RIGHT);

		uploadStatus.setFont(uploadStatus.getFont().deriveFont(Font.BOLD));

		progressBar = new JProgressBar() {
			private static final long serialVersionUID = -2846842560241143992L;

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				if (!progressBar.isIndeterminate()) {
					Dimension dim = uploadStatus.getPreferredSize();
					uploadStatus.paint(g.create(getSize().width / 2 - dim.width / 2,
							heightProgressBar / 2 - dim.height / 2, dim.width, dim.height));
				}
			}
		};
		progressBar.setIndeterminate(indeterminate);
		progressBar.setPreferredSize(new Dimension(size.width, heightProgressBar));
		progressBar.setBorder(BorderFactory.createEmptyBorder());

		background.add(versionLabel, BorderLayout.NORTH);
		background.add(nameLabel, BorderLayout.CENTER);
		background.add(label, BorderLayout.SOUTH);

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
			image = ImageIO.read(ProgressBarLauncher.class.getResourceAsStream("/background.jpg"));
		} catch (IOException e) {
			log.warn("can't load image", e);
		}
		return image;
	}

	@Subscribe
	public void messageToSpeed(DownloaderStatus status) {
		if (progressBar.isIndeterminate()) {
			SwingUtilities.invokeLater(() -> {
				progressBar.setIndeterminate(false);
				int uploaded = (int) status.getDownloadSize() / (1024 * 1024);
				int allUpload = (int) status.getAllDownloadSize() / (1024 * 1024);
				uploadStatus.setText(String.format("%s %s/%s %s ", resourceBundle.getString("uploading"), uploaded,
						allUpload, resourceBundle.getString("mb")));
				Dimension dim = uploadStatus.getPreferredSize();
				uploadStatus.setBounds(getSize().width / 2 - dim.width / 2, heightProgressBar / 2 - dim.height / 2,
						dim.width, dim.height);
				progressBar.setMaximum(allUpload);
				progressBar.setValue(uploaded);

			});
		}
	}

}
