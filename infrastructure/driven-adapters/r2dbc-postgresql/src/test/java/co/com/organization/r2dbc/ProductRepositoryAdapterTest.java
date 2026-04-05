package co.com.organization.r2dbc;

import co.com.organization.model.product.Product;
import co.com.organization.r2dbc.entity.ProductEntity;
import co.com.organization.r2dbc.helper.ProductConstants;
import co.com.organization.r2dbc.mapper.ProductMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductRepositoryAdapterTest {
    @Mock
    private ProductRepository repository;

    private final ProductMapper mapper = Mappers.getMapper(ProductMapper.class);

    private ProductRepositoryAdapter productRepositoryAdapter;

    private static final UUID PRODUCT_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    private static final String PRODUCT_ID = PRODUCT_UUID.toString();
    private static final String INVALID_ID = "not-a-valid-uuid";

    private Product product;
    private ProductEntity productEntity;

    @BeforeEach
    void setUp() {
        productRepositoryAdapter = new ProductRepositoryAdapter(repository, mapper);

        product = Product.builder()
                .id(PRODUCT_ID)
                .name("Test Product")
                .build();

        productEntity = ProductEntity.builder()
                .id(PRODUCT_UUID)
                .name("Test Product")
                .build();
    }

    // =========================================================
    // save
    // =========================================================

    @Nested
    @DisplayName("save")
    class SaveTests {

        @Test
        @DisplayName("Happy Path: should return success message when product is saved correctly")
        void saveSuccessfullyReturnsSuccessMessage() {
            Product productWithOutId = Product.builder()
                    .name("Test Product")
                    .build();
            when(repository.save(any(ProductEntity.class))).thenReturn(Mono.just(productEntity));
            ArgumentCaptor<ProductEntity> captor = ArgumentCaptor.forClass(ProductEntity.class);

            StepVerifier.create(productRepositoryAdapter.save(Mono.just(productWithOutId)))
                    .expectNextMatches(message -> message.equals(
                            String.format(ProductConstants.MSG_PRODUCT_SAVE_SUCCESSFULLY, productEntity.id())))
                    .verifyComplete();
            verify(repository).save(captor.capture());
            assertThat(captor.getValue().id()).isNotNull();
        }

        @Test
        @DisplayName("Error Path: should propagate error when repository fails to save")
        void saveErrorRepositoryPropagatesRepositoryError() {
            when(repository.save(any(ProductEntity.class)))
                    .thenReturn(Mono.error(new RuntimeException("DB connection error")));

            StepVerifier.create(productRepositoryAdapter.save(Mono.just(product)))
                    .expectErrorMatches(error -> error instanceof RuntimeException &&
                            error.getMessage().equals("DB connection error"))
                    .verify();
        }
    }

    // =========================================================
    // getProductById
    // =========================================================

    @Nested
    @DisplayName("getProductById")
    class GetProductByIdTests {

        @Test
        @DisplayName("Happy Path: should return ProductEntity when product is found")
        void getProductByIdSuccessfullyReturnsProductEntity() {
            when(repository.findById(PRODUCT_UUID)).thenReturn(Mono.just(productEntity));

            StepVerifier.create(productRepositoryAdapter.getProductById(PRODUCT_ID))
                    .expectNext(productEntity)
                    .verifyComplete();
        }

        @Test
        @DisplayName("Empty Path: should return empty when product is not found")
        void getProductByIdProductNotFoundReturnsEmpty() {
            when(repository.findById(PRODUCT_UUID)).thenReturn(Mono.empty());

            StepVerifier.create(productRepositoryAdapter.getProductById(PRODUCT_ID))
                    .verifyComplete();
        }

        @Test
        @DisplayName("Error Path: should throw IllegalArgumentException when productId is not a valid UUID")
        void getProductByIdErrorInRepositoryThrowsIllegalArgumentException() {

            assertThrows(IllegalArgumentException.class,
                    () -> productRepositoryAdapter.getProductById(INVALID_ID));
        }
    }

    // =========================================================
    // update
    // =========================================================

    @Nested
    @DisplayName("update")
    class UpdateTests {

        @Test
        @DisplayName("Happy Path: should return success message when product is updated correctly")
        void updateSuccessfullyReturnsSuccessMessage() {
            when(repository.save(any(ProductEntity.class))).thenReturn(Mono.just(productEntity));

            StepVerifier.create(productRepositoryAdapter.update(Mono.just(productEntity)))
                    .expectNext(ProductConstants.MSG_NAME_UPDATED_SUCCESSFULLY)
                    .verifyComplete();
        }

        @Test
        @DisplayName("Empty Path: should return empty when input Mono is empty")
        void updateEmptyResponseReturnsEmpty() {
            StepVerifier.create(productRepositoryAdapter.update(Mono.empty()))
                    .verifyComplete();
        }

        @Test
        @DisplayName("Error Path: should propagate error when repository fails to update")
        void updateErrorInRepositoryPropagatesRepositoryError() {
            when(repository.save(any(ProductEntity.class)))
                    .thenReturn(Mono.error(new RuntimeException("DB connection error")));

            StepVerifier.create(productRepositoryAdapter.update(Mono.just(productEntity)))
                    .expectErrorMatches(error -> error instanceof RuntimeException &&
                            error.getMessage().equals("DB connection error"))
                    .verify();
        }
    }

    // =========================================================
    // getProductsByIdList
    // =========================================================

    @Nested
    @DisplayName("getProductsByIdList")
    class GetProductsByIdListTests {

        @Test
        @DisplayName("Happy Path: should return mapped Product list when IDs are found")
        void getProductsByIdListSuccessfullyReturnsProductList() {
            when(repository.findAllById(ArgumentMatchers.<Publisher<UUID>>any())).thenReturn(Flux.just(productEntity));

            StepVerifier.create(productRepositoryAdapter.getProductsByIdList(Flux.just(PRODUCT_ID)))
                    .expectNext(product)
                    .verifyComplete();
        }

        @Test
        @DisplayName("Empty Path: should return empty Flux when no products match the ID list")
        void getProductsByIdListEmptyResponseReturnsEmptyFlux() {
            when(repository.findAllById(ArgumentMatchers.<Publisher<UUID>>any())).thenReturn(Flux.empty());

            StepVerifier.create(productRepositoryAdapter.getProductsByIdList(Flux.just(PRODUCT_ID)))
                    .verifyComplete();
        }

        @Test
        @DisplayName("Error Path: should propagate error when repository fails")
        void getProductsByIdListErrorInRepositoryPropagatesRepositoryError() {
            when(repository.findAllById(ArgumentMatchers.<Publisher<UUID>>any()))
                    .thenReturn(Flux.error(new RuntimeException("DB connection error")));

            StepVerifier.create(productRepositoryAdapter.getProductsByIdList(Flux.just(PRODUCT_ID)))
                    .expectErrorMatches(error -> error instanceof RuntimeException &&
                            error.getMessage().equals("DB connection error"))
                    .verify();
        }
    }

    // =========================================================
    // validateExistence
    // =========================================================

    @Nested
    @DisplayName("validateExistence")
    class ValidateExistenceTests {

        @Test
        @DisplayName("Happy Path: should return true when product exists")
        void validateExistenceSuccessfullyReturnsTrue() {
            when(repository.existsById(PRODUCT_UUID)).thenReturn(Mono.just(true));

            StepVerifier.create(productRepositoryAdapter.validateExistence(PRODUCT_ID))
                    .expectNext(true)
                    .verifyComplete();
        }

        @Test
        @DisplayName("Empty Path: should return false when product does not exist")
        void validateExistenceProductNotFoundReturnsFalse() {
            when(repository.existsById(PRODUCT_UUID)).thenReturn(Mono.just(false));

            StepVerifier.create(productRepositoryAdapter.validateExistence(PRODUCT_ID))
                    .expectNext(false)
                    .verifyComplete();
        }

        @Test
        @DisplayName("Error Path: should throw IllegalArgumentException when productId is not a valid UUID")
        void validateExistenceErrorInRepositoryThrowsIllegalArgumentException() {
             assertThrows(IllegalArgumentException.class,
                    () -> productRepositoryAdapter.validateExistence(INVALID_ID));
        }
    }
    }
