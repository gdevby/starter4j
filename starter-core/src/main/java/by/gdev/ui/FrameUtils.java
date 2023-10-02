package by.gdev.ui;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FrameUtils {
	
	public static void setFavicons(JFrame frame){
		try {
			List<Image> favicons = new ArrayList<>();
			int[] sizes = new int[] { 256, 128, 96, 64, 48, 32, 24, 16 };
			StringBuilder loadedBuilder = new StringBuilder();
			for (int i : sizes) {
				BufferedImage image = ImageIO.read(StarterStatusFrame.class.getResourceAsStream("/logo.jpg"));
				if (image == null)
					continue;
				loadedBuilder.append(", ").append(i).append("px");
				favicons.add(image);
			}
			String loaded = loadedBuilder.toString();
			if (loaded.isEmpty())
				log.info("No favicon is loaded.");
			else
				log.info("Favicons loaded:", loaded.substring(2));
				frame.setIconImages(favicons);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
