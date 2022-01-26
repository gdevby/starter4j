package by.gdev.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import by.gdev.model.AppConfig;
import by.gdev.model.AppLocalConfig;
import by.gdev.model.StarterAppConfig;
import by.gdev.util.DesktopUtil;
import by.gdev.util.OSInfo.OSType;
import by.gdev.utils.service.FileMapperService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UpdateFrame extends JFrame {

	private static final long serialVersionUID = 2832387031191814036L;
	private AtomicInteger userChoose = new AtomicInteger();

	public UpdateFrame(JFrame progressFrame, ResourceBundle resourceBundle, AppLocalConfig appLocalConfig,
			AppConfig remoteAppConfig, StarterAppConfig starterAppConfig, FileMapperService fileMapperService,
			OSType osType) {
		progressFrame.setVisible(false);
		setResizable(false);
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		int width = gd.getDisplayMode().getWidth();
		int height = gd.getDisplayMode().getHeight();

		setSize(new Dimension(width / 4, height / 5));
		this.setLocation(width / 2 - this.getSize().width / 2, height / 2 - this.getSize().height / 2);
		progressFrame.setVisible(false);
		JPanel p = new JPanel(new BorderLayout(0, 0));
		p.setBackground(Color.WHITE);
		p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		String link = String.join("/", starterAppConfig.getServerFile(), StarterAppConfig.APP_CHANGES_LOG);
		JLabel text = new JLabelHtmlWrapper(String.format(resourceBundle.getString("update.app"),
				appLocalConfig.getCurrentAppVersion(), remoteAppConfig.getAppVersion()));
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
		JCheckBox j = new JCheckBox(
				String.format(resourceBundle.getString("not.show.again"), remoteAppConfig.getAppVersion()));
		j.setAlignmentX(Component.CENTER_ALIGNMENT);
		j.addActionListener((e) -> {
			try {
				AppLocalConfig app = fileMapperService.read(StarterAppConfig.APP_STARTER_LOCAL_CONFIG,
						AppLocalConfig.class);
				app.setSkipUpdateVersion(remoteAppConfig.getAppVersion());
				fileMapperService.write(app, StarterAppConfig.APP_STARTER_LOCAL_CONFIG);
			} catch (IOException e1) {
				log.error("error", e1);
			}
		});
		text.addMouseListener(new MouseAdapter() {
			private Color c = text.getForeground();

			@Override
			public void mouseClicked(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e)) {
					DesktopUtil.openLink(osType, link);
				}
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				text.setForeground(Color.BLACK);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				text.setForeground(c);
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
