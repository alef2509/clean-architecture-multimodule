package io.github.alef2509.library.persistence;

import io.github.alef2509.library.application.gateway.UnitOfWork;
import io.github.alef2509.library.domain.LibraryException;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Supplier;

/**
 * The UnitOfWork port as a database transaction. Persistence failures caused by
 * concurrent requests (two members grabbing the same copy, a stale version) are
 * translated into a business-level {@code conflict}, so outer layers never see
 * Spring or JPA exceptions.
 */
@Component
class TransactionalUnitOfWork implements UnitOfWork {

    private final TransactionTemplate transactions;

    TransactionalUnitOfWork(PlatformTransactionManager transactionManager) {
        this.transactions = new TransactionTemplate(transactionManager);
    }

    @Override
    public <T> T execute(Supplier<T> work) {
        try {
            return transactions.execute(status -> work.get());
        } catch (DataIntegrityViolationException | ConcurrencyFailureException e) {
            throw new LibraryException("conflict", "The resource was changed by a concurrent request, retry");
        }
    }
}
