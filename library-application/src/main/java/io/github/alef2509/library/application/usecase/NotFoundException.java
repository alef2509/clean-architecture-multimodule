package io.github.alef2509.library.application.usecase;

import io.github.alef2509.library.domain.LibraryException;

public class NotFoundException extends LibraryException {

    public NotFoundException(String what, Object id) {
        super("not-found", "%s %s not found".formatted(what, id));
    }
}
