package by.gdev.model;

import lombok.Data;

@Data
public class ValidationExceptionMessage {
	String message;
	
	public ValidationExceptionMessage(String message) {
		this.message = message;
	}
	
	public String printValidationMessage() {
		return message;
	}
	
}
