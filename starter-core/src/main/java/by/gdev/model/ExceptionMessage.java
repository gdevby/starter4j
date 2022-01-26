package by.gdev.model;

import lombok.Data;

@Data
public class ExceptionMessage {
	private String message;
	private String link;

	public ExceptionMessage(String message) {
		this.message = message;
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
