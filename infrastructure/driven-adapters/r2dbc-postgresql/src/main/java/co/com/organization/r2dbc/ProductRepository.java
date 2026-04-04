package co.com.organization.r2dbc;

import co.com.organization.r2dbc.entity.ProductEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface ProductRepository extends ReactiveCrudRepository<ProductEntity, UUID>, ReactiveQueryByExampleExecutor<ProductEntity> {

}
