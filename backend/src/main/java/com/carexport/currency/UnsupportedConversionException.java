package com.carexport.currency;

public class UnsupportedConversionException extends RuntimeException {
    public UnsupportedConversionException(CurrencyCode from, CurrencyCode to) {
        super("Conversion from " + from + " to " + to + " is not supported.");
    }
}
