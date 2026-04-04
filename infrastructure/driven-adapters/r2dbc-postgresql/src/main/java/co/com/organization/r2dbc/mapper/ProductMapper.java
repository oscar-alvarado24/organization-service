package co.com.organization.r2dbc.mapper;

import co.com.organization.model.product.Product;
import co.com.organization.r2dbc.entity.ProductEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductEntity toEntity(Product product);
    Product toModel(ProductEntity entity);
}
