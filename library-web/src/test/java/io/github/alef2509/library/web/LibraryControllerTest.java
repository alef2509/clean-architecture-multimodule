package io.github.alef2509.library.web;

import io.github.alef2509.library.application.usecase.BorrowCopy;
import io.github.alef2509.library.application.usecase.ListMemberLoans;
import io.github.alef2509.library.application.usecase.RegisterBook;
import io.github.alef2509.library.application.usecase.RegisterMember;
import io.github.alef2509.library.application.usecase.RenewLoan;
import io.github.alef2509.library.application.usecase.ReserveBook;
import io.github.alef2509.library.application.usecase.ReturnCopy;
import io.github.alef2509.library.domain.LibraryException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** The web adapter in isolation: use cases are mocked at their input boundaries. */
@WebMvcTest(LibraryController.class)
class LibraryControllerTest {

    @SpringBootApplication
    static class WebOnly {
    }

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private RegisterBook registerBook;
    @MockitoBean
    private RegisterMember registerMember;
    @MockitoBean
    private BorrowCopy borrowCopy;
    @MockitoBean
    private ReturnCopy returnCopy;
    @MockitoBean
    private RenewLoan renewLoan;
    @MockitoBean
    private ReserveBook reserveBook;
    @MockitoBean
    private ListMemberLoans listMemberLoans;

    private final String loanJson = """
            {"memberId": "%s", "copyId": "%s"}""".formatted(UUID.randomUUID(), UUID.randomUUID());

    @Test
    void borrowReturns201WithTheUseCaseResponse() throws Exception {
        given(borrowCopy.execute(any())).willReturn(
                new BorrowCopy.Response(UUID.randomUUID(), UUID.randomUUID(), LocalDate.of(2026, 3, 15)));

        mvc.perform(post("/api/v1/loans").contentType(MediaType.APPLICATION_JSON).content(loanJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dueOn").value("2026-03-15"));
    }

    @Test
    void businessRulesBecomeProblemDetails() throws Exception {
        given(borrowCopy.execute(any())).willThrow(new LibraryException("loan-limit", "limit reached"));

        mvc.perform(post("/api/v1/loans").contentType(MediaType.APPLICATION_JSON).content(loanJson))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.type").value("urn:problem-type:library:loan-limit"));
    }

    @Test
    void concurrentConflictsAre409() throws Exception {
        given(borrowCopy.execute(any())).willThrow(new LibraryException("conflict", "retry"));

        mvc.perform(post("/api/v1/loans").contentType(MediaType.APPLICATION_JSON).content(loanJson))
                .andExpect(status().isConflict());
    }

    @Test
    void invalidInputNeverReachesTheUseCase() throws Exception {
        mvc.perform(post("/api/v1/books").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isbn\": \"\", \"title\": \"\", \"copies\": 0}"))
                .andExpect(status().isBadRequest());
    }
}
