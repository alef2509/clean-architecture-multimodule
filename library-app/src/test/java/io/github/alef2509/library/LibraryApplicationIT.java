package io.github.alef2509.library;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;

/** All modules assembled, real PostgreSQL: the adapters honour the gateways' contracts. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
class LibraryApplicationIT {

    @Autowired
    private TestRestTemplate rest;

    @Test
    void borrowReserveReturnAndHoldThroughTheWholeStack() {
        List<UUID> copies = registerBook("9780321125217", 1); // Domain-Driven Design
        UUID ada = member("Ada");
        UUID bob = member("Bob");

        ResponseEntity<JsonNode> loan = post("/api/v1/loans", Map.of("memberId", ada, "copyId", copies.getFirst()));
        assertThat(loan.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        UUID loanId = UUID.fromString(loan.getBody().get("loanId").asText());

        ResponseEntity<JsonNode> reservation = post("/api/v1/reservations", Map.of("memberId", bob, "isbn", "978-0-321-12521-7"));
        assertThat(reservation.getBody().get("position").asInt()).isEqualTo(1);

        ResponseEntity<JsonNode> renewal = post("/api/v1/loans/" + loanId + "/renew", null);
        assertThat(renewal.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(renewal.getBody().get("type").asText()).isEqualTo("urn:problem-type:library:book-reserved");

        JsonNode returned = post("/api/v1/loans/" + loanId + "/return", null).getBody();
        assertThat(returned.get("copyStatus").asText()).isEqualTo("ON_HOLD");
        assertThat(returned.get("heldFor").asText()).isEqualTo(bob.toString());

        assertThat(post("/api/v1/loans", Map.of("memberId", bob, "copyId", copies.getFirst())).getStatusCode())
                .isEqualTo(HttpStatus.CREATED);
        assertThat(rest.getForObject("/api/v1/members/{id}/loans", JsonNode.class, bob)).hasSize(1);
        assertThat(rest.getForObject("/api/v1/members/{id}/loans", JsonNode.class, ada)).isEmpty();
    }

    @Test
    void membershipLimitIsEnforced() {
        List<UUID> copies = registerBook("9780134494166", 4); // Clean Architecture
        UUID ada = member("Ada");
        copies.subList(0, 3).forEach(c -> post("/api/v1/loans", Map.of("memberId", ada, "copyId", c)));

        ResponseEntity<JsonNode> fourth = post("/api/v1/loans", Map.of("memberId", ada, "copyId", copies.get(3)));

        assertThat(fourth.getBody().get("type").asText()).isEqualTo("urn:problem-type:library:loan-limit");
    }

    /** Two members, one copy, same instant: exactly one loan is created. */
    @Test
    void simultaneousBorrowsOfOneCopyCreateOneLoan() throws Exception {
        UUID copy = registerBook("9780596007126", 1).getFirst(); // Head First Design Patterns
        List<UUID> members = List.of(member("A"), member("B"), member("C"), member("D"));

        List<HttpStatus> statuses;
        try (ExecutorService pool = Executors.newFixedThreadPool(members.size())) {
            List<Future<HttpStatus>> futures = pool.invokeAll(members.stream().<Callable<HttpStatus>>map(m -> () ->
                    HttpStatus.valueOf(post("/api/v1/loans", Map.of("memberId", m, "copyId", copy))
                            .getStatusCode().value())).toList());
            statuses = futures.stream().map(f -> {
                try {
                    return f.get();
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }).toList();
        }

        assertThat(statuses).filteredOn(HttpStatus.CREATED::equals).hasSize(1);
        assertThat(statuses).filteredOn(s -> s != HttpStatus.CREATED)
                .allMatch(s -> s == HttpStatus.CONFLICT || s == HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void invalidIsbnIsABadRequest() {
        ResponseEntity<JsonNode> response = post("/api/v1/books",
                Map.of("isbn", "9780000000000", "title", "Fake", "copies", 1));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("type").asText()).isEqualTo("urn:problem-type:library:invalid-isbn");
    }

    private List<UUID> registerBook(String isbn, int copies) {
        JsonNode body = post("/api/v1/books", Map.of("isbn", isbn, "title", "Book " + isbn, "author", "Someone",
                "copies", copies)).getBody();
        return StreamSupport.stream(body.get("copyIds").spliterator(), false)
                .map(n -> UUID.fromString(n.asText())).toList();
    }

    private UUID member(String name) {
        return UUID.fromString(post("/api/v1/members", Map.of("name", name, "membership", "STANDARD"))
                .getBody().get("memberId").asText());
    }

    private ResponseEntity<JsonNode> post(String path, Object body) {
        return rest.postForEntity(path, body, JsonNode.class);
    }
}
