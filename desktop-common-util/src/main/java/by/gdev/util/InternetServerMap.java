package by.gdev.util;

import java.util.HashMap;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InternetServerMap extends HashMap<String, Boolean> {
	private static final long serialVersionUID = 1L;

	public boolean isSkippedURL(String url) {
		if (keySet().stream().filter(e -> url.contains(e) && !this.get(e)).findAny().isPresent()) {
			log.debug("skip request to server {}", url);
			return true;
		} else
			return false;
	}

}
