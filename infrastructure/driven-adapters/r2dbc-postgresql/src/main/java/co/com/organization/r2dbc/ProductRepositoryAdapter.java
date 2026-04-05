package co.com.organization.r2dbc;

import co.com.organization.model.product.Product;
import co.com.organization.r2dbc.entity.ProductEntity;
import co.com.organization.r2dbc.helper.ProductAdapterOperations;
import co.com.organization.r2dbc.helper.ProductConstants;
import co.com.organization.r2dbc.mapper.ProductMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public class ProductRepositoryAdapter extends ProductAdapterOperations<
        ProductEntity,
        Product,
        UUID,
        ProductRepository
> {
    private final ProductMapper mapper;
    public ProductRepositoryAdapter(ProductRepository repository, ProductMapper mapper) {
        super(repository);
        this.mapper = mapper;
    }

    @Override
    protected Product toModelImpl(ProductEntity entity) {
        return mapper.toModel(entity);
    }

    @Override
    protected ProductEntity toEntityImpl(Product data) {
        return mapper.toEntity(data);
    }

    public Mono<String> save(Mono<Product> product) {
        return product.flatMap(productSaved ->{
            productSaved.setId(UUID.randomUUID().toString());
            return repository.save(toEntity(productSaved));})
               .map(productEntity -> String.format(ProductConstants.MSG_PRODUCT_SAVE_SUCCESSFULLY,productEntity.id()));
    }

    public Mono<ProductEntity> getProductById(String productId) {
        return repository.findById(UUID.fromString(productId));
    }

    public Mono<String > update(Mono<ProductEntity> productEntity) {
        return productEntity.flatMap(repository::save).map(product -> ProductConstants.MSG_NAME_UPDATED_SUCCESSFULLY);
    }

    public Flux<Product> getProductsByIdList(Flux<String> idList) {
        return repository.findAllById(idList.map(UUID::fromString))
                .map(this::toModel);

    }

    public Mono<Boolean> validateExistence(String productId) {
        return repository.existsById(UUID.fromString(productId));
    }
}
