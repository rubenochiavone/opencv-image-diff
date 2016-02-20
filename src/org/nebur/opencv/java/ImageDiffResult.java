package org.nebur.opencv.java;

import java.io.File;

public class ImageDiffResult {

	private boolean equals;
	private String errorMessage;
	private Exception exception;
	private File diffResult;
	
	public ImageDiffResult() {
	}

	public boolean isEquals() {
		return equals;
	}

	protected void setEquals(boolean equals) {
		this.equals = equals;
	}
	
	public Exception getException() {
		return exception;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
	
	protected void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	protected void setException(Exception exception) {
		this.exception = exception;
	}

	public File getDiffResult() {
		return diffResult;
	}

	protected void setDiffResult(File diffResult) {
		this.diffResult = diffResult;
	}
}
