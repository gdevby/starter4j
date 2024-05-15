package by.gdev.model;

import lombok.Data;

@Data
public class ExceptionMessage {
	private String message;
	private String link;
	private Throwable error;
	private boolean logButton;

	public ExceptionMessage(String message) {
		this.message = message;
	}

	public ExceptionMessage(String message, Throwable t) {
		this.message = message;
		this.error = t;
	}

	public ExceptionMessage(String message, Throwable t, boolean logButton) {
		this(message, t);
		this.logButton = logButton;
	}

	public ExceptionMessage(String message, String link) {
		super();
		this.message = message;
		this.link = link;
	}

	public ExceptionMessage(String message, String link, boolean logButton) {
		this(message, link);
		this.logButton = logButton;
	}

	public String printValidationMessage() {
		return message;
	}
}
