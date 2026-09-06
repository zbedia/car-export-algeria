package com.carexport.exception;

/**
 * Centralized user-facing error messages, so they can be reviewed and
 * translated in one place instead of being scattered across handlers.
 */
public final class ErrorMessages {

    public static final String BAD_REQUEST = "Bad Request";
    public static final String CONFLICT = "Conflict";
    public static final String INTERNAL_SERVER_ERROR = "Internal Server Error";

    public static final String INVALID_PARAMETER = "Parameter '%s' is invalid.";

    public static final String OPTIMISTIC_LOCKING_CONFLICT =
            "The listing was modified by another request. Please reload and try again.";
    public static final String DATA_INTEGRITY_VIOLATION =
            "The operation conflicts with a concurrent change. Please retry.";
    public static final String UNEXPECTED_ERROR = "An unexpected error occurred.";

    public static final String UNHANDLED_EXCEPTION = "Unhandled exception on {} {}";

    private ErrorMessages() {
    }
}
