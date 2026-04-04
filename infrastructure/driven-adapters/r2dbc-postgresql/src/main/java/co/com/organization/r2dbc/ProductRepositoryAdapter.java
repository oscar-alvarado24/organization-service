package co.com.organization.r2dbc;

import co.com.organization.model.product.Product;
import co.com.organization.r2dbc.entity.ProductEntity;
import co.com.organization.r2dbc.helper.ProductAdapterOperations;
import co.com.organization.r2dbc.mapper.ProductMapper;
import org.springframework.stereotype.Repository;

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
}
