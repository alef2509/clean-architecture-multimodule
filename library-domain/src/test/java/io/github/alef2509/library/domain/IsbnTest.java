package io.github.alef2509.library.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IsbnTest {

    @Test
    void acceptsValidIsbn13IgnoringHyphens() {
        assertThat(new Isbn("978-0-13-468599-1").value()).isEqualTo("9780134685991");
    }

    @Test
    void rejectsWrongChecksumOrFormat() {
        assertThatThrownBy(() -> new Isbn("9780134685992")).isInstanceOf(LibraryException.class);
        assertThatThrownBy(() -> new Isbn("12345")).isInstanceOf(LibraryException.class);
    }
}
