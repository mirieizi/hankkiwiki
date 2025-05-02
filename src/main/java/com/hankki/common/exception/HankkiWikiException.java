package com.hankki.common.exception;

public class HankkiWikiException extends RuntimeException {
	
	private final ExceptionStatus exceptionStatus;
	
	public HankkiWikiException(ExceptionStatus exceptionStatus) {
		super(exceptionStatus.getMessage());
		this.exceptionStatus = exceptionStatus;
	}
	
	public ExceptionStatus getInfo() {
		return exceptionStatus;
	}
	
	@Override
	public String getMessage() {
		return exceptionStatus.getMessage();
	}
	
}
