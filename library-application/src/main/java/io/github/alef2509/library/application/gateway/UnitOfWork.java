package io.github.alef2509.library.application.gateway;

import java.util.function.Supplier;

/**
 * Transaction boundary as an explicit port: a use case says "this is atomic"
 * without knowing how. The JPA adapter implements it with a database
 * transaction; tests use a pass-through.
 */
public interface UnitOfWork {

    <T> T execute(Supplier<T> work);
}
