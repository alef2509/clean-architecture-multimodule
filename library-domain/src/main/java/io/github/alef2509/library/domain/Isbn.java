package io.github.alef2509.library.domain;

/** ISBN-13 with checksum validation; hyphens and spaces are ignored. */
public record Isbn(String value) {

    public Isbn {
        if (value == null) {
            throw new LibraryException("invalid-isbn", "ISBN is required");
        }
        value = value.replace("-", "").replace(" ", "");
        if (!value.matches("\\d{13}") || !validChecksum(value)) {
            throw new LibraryException("invalid-isbn", "Invalid ISBN-13: " + value);
        }
    }

    private static boolean validChecksum(String digits) {
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            sum += (digits.charAt(i) - '0') * (i % 2 == 0 ? 1 : 3);
        }
        return (10 - sum % 10) % 10 == digits.charAt(12) - '0';
    }

    @Override
    public String toString() {
        return value;
    }
}
