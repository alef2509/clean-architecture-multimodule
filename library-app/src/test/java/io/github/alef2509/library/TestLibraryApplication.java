package io.github.alef2509.library;

import org.springframework.boot.SpringApplication;

/** {@code ./mvnw -pl library-app spring-boot:test-run}: the app against a throwaway PostgreSQL. */
public class TestLibraryApplication {

    public static void main(String[] args) {
        SpringApplication.from(LibraryApplication::main).with(TestcontainersConfiguration.class).run(args);
    }
}
