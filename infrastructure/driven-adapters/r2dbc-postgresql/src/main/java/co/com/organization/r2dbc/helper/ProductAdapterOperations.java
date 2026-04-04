package co.com.organization.r2dbc.helper;

import org.reactivecommons.utils.ObjectMapper;
import org.springframework.data.domain.Example;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.ParameterizedType;
import java.util.function.Function;

public abstract class ProductAdapterOperations<E, M, I, R extends ReactiveCrudRepository<E, I> & ReactiveQueryByExampleExecutor<E>> {
    protected R repository;

    @SuppressWarnings("unchecked")
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
