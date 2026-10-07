package io.github.alef2509.library.web;

import io.github.alef2509.library.domain.LibraryException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;

/** Business rule codes become RFC 9457 problem types. */
@RestControllerAdvice
class LibraryExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(LibraryException.class)
    ProblemDetail handle(LibraryException e) {
        HttpStatus status = switch (e.code()) {
            case "not-found" -> HttpStatus.NOT_FOUND;
            case "conflict" -> HttpStatus.CONFLICT;
            case "invalid-isbn", "invalid-membership", "invalid-copies", "invalid-book", "invalid-member" ->
                    HttpStatus.BAD_REQUEST;
            default -> HttpStatus.UNPROCESSABLE_ENTITY;
        };
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, e.getMessage());
        problem.setType(URI.create("urn:problem-type:library:" + e.code()));
        return problem;
    }
}
