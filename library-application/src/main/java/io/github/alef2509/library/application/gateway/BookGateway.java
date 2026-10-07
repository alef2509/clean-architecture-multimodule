package io.github.alef2509.library.application.gateway;

import io.github.alef2509.library.domain.Book;
import io.github.alef2509.library.domain.Isbn;

import java.util.Optional;

public interface BookGateway {

    void save(Book book);

    Optional<Book> findByIsbn(Isbn isbn);
}
