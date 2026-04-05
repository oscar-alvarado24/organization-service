package co.com.organization.r2dbc.helper;

import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public abstract class ProductAdapterOperations<E, M, I, R extends ReactiveCrudRepository<E, I> & ReactiveQueryByExampleExecutor<E>> {
    protected R repository;

    protected ProductAdapterOperations(R repository) {
        this.repository = repository;
    }
    protected M toModel(E entity) {
        return entity != null ? toModelImpl(entity) : null;
    }
    protected E toEntity(M model) {
        return model != null ? toEntityImpl(model) : null;
    }
    protected abstract M toModelImpl(E entity) ;
    protected abstract E toEntityImpl(M data);
}
