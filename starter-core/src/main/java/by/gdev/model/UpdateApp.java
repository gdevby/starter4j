package by.gdev.model;

import java.util.List;
import java.util.Map;

import by.gdev.util.model.download.Metadata;
import lombok.Data;

@Data
public class UpdateApp {
	/*
	 * servers with update
	 */
	private List<String> urls;
	private Map<String, Metadata> map;
}
