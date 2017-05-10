package org.aksw.mlbenchmark;

/**
 * A configloader exception
 */
public class ConfigLoaderException extends Exception {
	private static final long serialVersionUID = 4209408106864708557L;
	public ConfigLoaderException(String message, Throwable cause) { super(message, cause); }
	public ConfigLoaderException(String message) { super(message); }
}
