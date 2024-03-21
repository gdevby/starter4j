package by.gdev.model;

import lombok.Data;

@Data
public class ExceptionMessage {
	private String message;
	private String link;
	private Throwable error;

	public ExceptionMessage(String message) {
		this.message = message;
	}

	public ExceptionMessage(String message, Throwable t) {
		this.message = message;
		this.error = t;
	}

	public ExceptionMessage(String message, String link) {
		super();
		this.message = message;
		this.link = link;
	}

	public String printValidationMessage() {
		return message;
	}
}
