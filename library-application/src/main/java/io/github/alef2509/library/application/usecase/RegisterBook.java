package io.github.alef2509.library.application.usecase;

import java.util.List;
import java.util.UUID;

/** Input boundary. */
public interface RegisterBook {

    Response execute(Request request);

    record Request(String isbn, String title, String author, int copies) {
    }

    record Response(String isbn, List<UUID> copyIds) {
    }
}
