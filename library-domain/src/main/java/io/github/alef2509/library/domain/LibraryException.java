package io.github.alef2509.library.domain;

/** A business rule was violated. The code identifies the rule for clients. */
public class LibraryException extends RuntimeException {

    private final String code;

    public LibraryException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String code() {
        return code;
    }
}
