package com.carexport.exception;

public class ScrapingException extends RuntimeException {
    public ScrapingException(String source, Throwable cause) {
        super("Error while scraping " + source, cause);
    }
}
