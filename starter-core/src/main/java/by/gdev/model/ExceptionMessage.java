package by.gdev.model;

import lombok.Data;

@Data

public class ExceptionMessage {
	String message;
	
	public ExceptionMessage(String message) {
		this.message = message;
	}
	
	public String printValidationMessage() {
		return message;
	}
	
}
