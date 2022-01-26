package by.gdev.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import by.gdev.model.AppLocalConfig;
import by.gdev.model.StarterAppConfig;
import by.gdev.util.DesktopUtil;
import by.gdev.utils.service.FileMapperService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UpdateFrame extends JFrame {

	private static final long serialVersionUID = 2832387031191814036L;
	private AtomicInteger userChoose = new AtomicInteger();

	public UpdateFrame(JFrame progressFrame, ResourceBundle resourceBundle, String currentVersion, String newVersion,
			FileMapperService fileMapperService) {
		progressFrame.setVisible(false);
		setResizable(false);
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		int width = gd.getDisplayMode().getWidth();
		int height = gd.getDisplayMode().getHeight();

		setSize(new Dimension(width / 4, height / 5));
		this.setLocation(width / 2 - this.getSize().width / 2, height / 2 - this.getSize().height / 2);
		progressFrame.setVisible(false);
		BufferedImage image = getImage();
		JPanel p = new JPanel(new BorderLayout(0, 0)) {
			private static final long serialVersionUID = 1L;

			@Override
			public void paint(Graphics g) {
				if (Objects.nonNull(image))
					g.drawImage(image, 0, 0, null);
				super.paint(g);
			}
		};
		p.setOpaque(false);
		p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JLabel text = new JLabelHtmlWrapper(
				String.format(resourceBundle.getString("update.app"), currentVersion, newVersion));
		text.setFont(text.getFont().deriveFont(Font.BOLD));
		text.setHorizontalAlignment(JLabel.CENTER);
		JPanel buttonPanel = new JPanel(new FlowLayout());
		JPanel verticalPanel = new JPanel();
		verticalPanel.setOpaque(false);
		BoxLayout boxLayout = new BoxLayout(verticalPanel, BoxLayout.Y_AXIS);
		verticalPanel.setLayout(boxLayout);
		addButton(new JButton(resourceBundle.getString("skip")), buttonPanel, 1);
		addButton(new JButton(resourceBundle.getString("update")), buttonPanel, 2);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		verticalPanel.add(buttonPanel);
		JCheckBox j = new JCheckBox(String.format(resourceBundle.getString("not.show.again"),newVersion));
		j.setAlignmentX(Component.CENTER_ALIGNMENT);
		j.addActionListener((e) -> {
			try {
				AppLocalConfig app = fileMapperService.read(StarterAppConfig.APP_STARTER_LOCAL_CONFIG,
						AppLocalConfig.class);
				app.setSkipUpdateVersion(newVersion);
				fileMapperService.write(app, StarterAppConfig.APP_STARTER_LOCAL_CONFIG);
			} catch (IOException e1) {
				log.error("error",e1);
			}
		});
		verticalPanel.add(j);

		buttonPanel.setOpaque(false);

		p.add(text, BorderLayout.CENTER);
		p.add(verticalPanel, BorderLayout.SOUTH);
		this.add(p);
		
		setVisible(true);
		
		
		while (isVisible()) {
			DesktopUtil.sleep(100);
		}
		progressFrame.setVisible(true);
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

	private void addButton(JButton b, JPanel panel, int code) {
		b.setAlignmentX(Component.CENTER_ALIGNMENT);
		b.addActionListener((e) -> {
			userChoose.set(code);
			setVisible(false);
		});
		panel.add(b);
	}

	public int getUserChoose() {
		return userChoose.get();
	}
}
