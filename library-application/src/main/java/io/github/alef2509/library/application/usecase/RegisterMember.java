package io.github.alef2509.library.application.usecase;

import java.util.UUID;

public interface RegisterMember {

    Response execute(Request request);

    record Request(String name, String membership) {
    }

    record Response(UUID memberId) {
    }
}
