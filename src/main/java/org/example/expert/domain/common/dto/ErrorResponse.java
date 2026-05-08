package org.example.expert.domain.common.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Getter;

@Getter
@JsonPropertyOrder({"status", "error", "message"})
public class ErrorResponse {

	private final int status;
	private final String error;
	private final String message;

	public ErrorResponse(int status, String error, String message) {
		this.status = status;
		this.error = error;
		this.message = message;
	}
}