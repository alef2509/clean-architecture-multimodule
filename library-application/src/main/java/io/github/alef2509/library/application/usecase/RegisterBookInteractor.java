package io.github.alef2509.library.application.usecase;

import io.github.alef2509.library.application.gateway.BookGateway;
import io.github.alef2509.library.application.gateway.CopyGateway;
import io.github.alef2509.library.application.gateway.UnitOfWork;
import io.github.alef2509.library.domain.Book;
import io.github.alef2509.library.domain.Copy;
import io.github.alef2509.library.domain.Isbn;
import io.github.alef2509.library.domain.LibraryException;

import java.util.List;
import java.util.stream.IntStream;

public class RegisterBookInteractor implements RegisterBook {

    private final BookGateway books;
    private final CopyGateway copies;
    private final UnitOfWork unitOfWork;

    public RegisterBookInteractor(BookGateway books, CopyGateway copies, UnitOfWork unitOfWork) {
        this.books = books;
        this.copies = copies;
        this.unitOfWork = unitOfWork;
    }

    @Override
    public Response execute(Request request) {
        if (request.copies() < 1 || request.copies() > 100) {
            throw new LibraryException("invalid-copies", "Register between 1 and 100 copies");
        }
        Isbn isbn = new Isbn(request.isbn());
        return unitOfWork.execute(() -> {
            if (books.findByIsbn(isbn).isEmpty()) {
                books.save(new Book(isbn, request.title(), request.author()));
            }
            List<Copy> added = IntStream.range(0, request.copies()).mapToObj(i -> Copy.newCopy(isbn)).toList();
            added.forEach(copies::save);
            return new Response(isbn.value(), added.stream().map(c -> c.id().value()).toList());
        });
    }
}
