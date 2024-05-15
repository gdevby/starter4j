package by.gdev.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LogResponse {
	String message;
	String link;
}
