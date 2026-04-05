package co.com.organization.r2dbc.helper;

import co.com.organization.model.product.Product;
import co.com.organization.r2dbc.ProductRepository;
import co.com.organization.r2dbc.entity.ProductEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ProductAdapterOperationsTest {

    @Mock
    private ProductRepository repository;

    private ProductAdapterOperations<ProductEntity, Product, UUID, ProductRepository> adapterOperations;

    private static final UUID PRODUCT_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

    private Product product;
    private ProductEntity productEntity;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(PRODUCT_UUID.toString())
                .name("Test Product")
                .build();

        productEntity = ProductEntity.builder()
                .id(PRODUCT_UUID)
                .name("Test Product")
                .build();

        adapterOperations = new ProductAdapterOperations<>(repository) {
            @Override
            protected Product toModelImpl(ProductEntity entity) {
                return Product.builder()
                        .id(entity.id().toString())
                        .name(entity.name())
                        .build();
            }

            @Override
            protected ProductEntity toEntityImpl(Product data) {
                return ProductEntity.builder()
                        .id(UUID.fromString(data.getId()))
                        .name(data.getName())
                        .build();
            }
        };
    }

    // =========================================================
    // toModel
    // =========================================================

    @Nested
    @DisplayName("toModel")
    class ToModelTests {

        @Test
        @DisplayName("Happy Path: should return mapped Product when entity is not null")
        void toModel_happyPath_returnsMappedProduct() {
            Product result = adapterOperations.toModel(productEntity);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(PRODUCT_UUID.toString());
            assertThat(result.getName()).isEqualTo(productEntity.name());
        }

        @Test
        @DisplayName("Empty Path: should return null when entity is null")
        void toModel_emptyPath_returnsNull() {
            Product result = adapterOperations.toModel(null);

            assertThat(result).isNull();
        }
    }

    // =========================================================
    // toEntity
    // =========================================================

    @Nested
    @DisplayName("toEntity")
    class ToEntityTests {

        @Test
        @DisplayName("Happy Path: should return mapped ProductEntity when model is not null")
        void toEntity_happyPath_returnsMappedEntity() {
            ProductEntity result = adapterOperations.toEntity(product);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(PRODUCT_UUID);
            assertThat(result.name()).isEqualTo(product.getName());
        }

        @Test
        @DisplayName("Empty Path: should return null when model is null")
        void toEntity_emptyPath_returnsNull() {
            ProductEntity result = adapterOperations.toEntity(null);

            assertThat(result).isNull();
        }
    }
}
