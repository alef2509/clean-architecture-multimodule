package io.github.alef2509.library.application.gateway;

import io.github.alef2509.library.domain.Copy;
import io.github.alef2509.library.domain.Ids.CopyId;
import io.github.alef2509.library.domain.Isbn;

import java.util.List;
import java.util.Optional;

public interface CopyGateway {

    void save(Copy copy);

    Optional<Copy> findById(CopyId id);

    List<Copy> findCopiesOf(Isbn isbn);
}
