package io.github.alef2509.library.application.usecase;

import java.util.UUID;

public interface ReserveBook {

    Response execute(Request request);

    record Request(UUID memberId, String isbn) {
    }

    /** @param position place in the queue, 1 = next */
    record Response(UUID reservationId, int position) {
    }
}
