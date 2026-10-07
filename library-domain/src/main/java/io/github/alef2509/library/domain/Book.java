package io.github.alef2509.library.domain;

public record Book(Isbn isbn, String title, String author) {

    public Book {
        if (title == null || title.isBlank()) {
            throw new LibraryException("invalid-book", "Title is required");
        }
    }
}
