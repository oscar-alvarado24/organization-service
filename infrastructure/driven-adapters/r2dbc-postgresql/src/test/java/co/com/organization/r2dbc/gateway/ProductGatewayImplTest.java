package co.com.organization.r2dbc.gateway;

import co.com.organization.exception.ChangeProductNameFailed;
import co.com.organization.exception.CreateProductFailed;
import co.com.organization.exception.GetProductsByIdListFailed;
import co.com.organization.exception.ProductNotFoundException;
import co.com.organization.exception.ValidateExistenceFailed;
import co.com.organization.model.product.Product;
import co.com.organization.r2dbc.ProductRepositoryAdapter;
import co.com.organization.r2dbc.entity.ProductEntity;
import co.com.organization.r2dbc.helper.ProductConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductGatewayImplTest {

    @Mock
    private ProductRepositoryAdapter productRepositoryAdapter;

    @InjectMocks
    private ProductGatewayImpl productGatewayImpl;

    private Product product;
    private ProductEntity productEntity;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id("product-id-1")
                .name("Test Product")
                .build();

        productEntity = ProductEntity.builder()
                .id(UUID.randomUUID())
                .name("Test Product")
                .build();
    }

    // =========================================================
    // createProduct
    // =========================================================

    @Nested
    @DisplayName("createProduct")
    class CreateProductTests {

        @Test
        @DisplayName("Happy Path: should return product ID when save is successful")
        void createProductReturnsProductIdSuccessfully() {
            String response = String.format(ProductConstants.MSG_PRODUCT_SAVE_SUCCESSFULLY, productEntity.id());
            when(productRepositoryAdapter.save(any()))
                    .thenReturn(Mono.just(response));

            StepVerifier.create(productGatewayImpl.createProduct(Mono.just(product)))
                    .expectNext(response)
                    .verifyComplete();
        }

        @Test
        @DisplayName("Empty Path: should throw CreateProductFailed when save returns empty")
        void createProductEmptyAdapterResponseThrowsCreateProductFailed() {
            when(productRepositoryAdapter.save(any()))
                    .thenReturn(Mono.empty());

            StepVerifier.create(productGatewayImpl.createProduct(Mono.just(product)))
                    .expectErrorMatches(error ->
                            error instanceof CreateProductFailed &&
                                    error.getMessage().equals(ProductConstants.MSG_ERROR_SAVED_PRODUCT))
                    .verify();
        }

        @Test
        @DisplayName("Error Path: should throw CreateProductFailed when save throws unexpected exception")
        void createProductWithErrorResponseThrowsCreateProductFailed() {
            when(productRepositoryAdapter.save(any()))
                    .thenReturn(Mono.error(new RuntimeException("DB connection error")));

            StepVerifier.create(productGatewayImpl.createProduct(Mono.just(product)))
                    .expectErrorMatches(error ->
                            error instanceof CreateProductFailed &&
                                    error.getMessage().equals(ProductConstants.MSG_ERROR_SAVED_PRODUCT))
                    .verify();
        }
    }

    // =========================================================
    // changeProductName
    // =========================================================

    @Nested
    @DisplayName("changeProductName")
    class ChangeProductNameTests {

        @Test
        @DisplayName("Happy Path: should return updated product ID when product exists and update succeeds")
        void changeProductNameShouldReturnsSuccessfullyUpdateMessage() {
            when(productRepositoryAdapter.getProductById("product-id-1"))
                    .thenReturn(Mono.just(productEntity));
            when(productRepositoryAdapter.update(any()))
                    .thenReturn(Mono.just(ProductConstants.MSG_NAME_UPDATED_SUCCESSFULLY));

            StepVerifier.create(productGatewayImpl.changeProductName("product-id-1", "New Name"))
                    .expectNext(ProductConstants.MSG_NAME_UPDATED_SUCCESSFULLY)
                    .verifyComplete();
        }

        @Test
        @DisplayName("Empty Path: should throw ProductNotFoundException when product does not exist")
        void changeProductNameEmptyResponseThrowsProductNotFoundException() {
            when(productRepositoryAdapter.getProductById("product-id-1"))
                    .thenReturn(Mono.empty());

            StepVerifier.create(productGatewayImpl.changeProductName("product-id-1", "New Name"))
                    .expectErrorMatches(error ->
                            error instanceof ProductNotFoundException &&
                                    error.getMessage().equals(ProductConstants.MSG_PRODUCT_NOT_FOUND))
                    .verify();
        }

        @Test
        @DisplayName("Error Path: should throw ChangeProductNameFailed when an unexpected exception occurs")
        void changeProductNameWithErrorResponseThrowsChangeProductNameFailed() {
            when(productRepositoryAdapter.getProductById("product-id-1"))
                    .thenReturn(Mono.error(new RuntimeException("DB connection error")));

            StepVerifier.create(productGatewayImpl.changeProductName("product-id-1", "New Name"))
                    .expectErrorMatches(error ->
                            error instanceof ChangeProductNameFailed &&
                                    error.getMessage().equals(ProductConstants.MSG_ERROR_CHANGE_PRODUCT_NAME))
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
        @DisplayName("Happy Path: should complete without error when product exists")
        void validateExistenceSuccessfully() {
            when(productRepositoryAdapter.validateExistence("product-id-1"))
                    .thenReturn(Mono.just(Boolean.TRUE));

            StepVerifier.create(productGatewayImpl.validateExistence("product-id-1"))
                    .verifyComplete();
        }

        @Test
        @DisplayName("Empty Path: should throw ProductNotFoundException when product does not exist")
        void validateExistenceProductNotFoundThrowsProductNotFoundException() {
            when(productRepositoryAdapter.validateExistence("product-id-1"))
                    .thenReturn(Mono.just(Boolean.FALSE));

            StepVerifier.create(productGatewayImpl.validateExistence("product-id-1"))
                    .expectErrorMatches(error ->
                            error instanceof ProductNotFoundException &&
                                    error.getMessage().equals(ProductConstants.MSG_PRODUCT_NOT_FOUND))
                    .verify();
        }

        @Test
        @DisplayName("Error Path: should throw ValidateExistenceFailed when an unexpected exception occurs")
        void validateExistenceErrorResponseThrowsValidateExistenceFailed() {
            when(productRepositoryAdapter.validateExistence("product-id-1"))
                    .thenReturn(Mono.error(new RuntimeException("DB connection error")));

            StepVerifier.create(productGatewayImpl.validateExistence("product-id-1"))
                    .expectErrorMatches(error ->
                            error instanceof ValidateExistenceFailed &&
                                    error.getMessage().equals(ProductConstants.MSG_ERROR_VALIDATING_PRODUCT_EXISTENCE))
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
        @DisplayName("Happy Path: should return all products matching the given ID list")
        void getProductsByIdListReturnsProductsSuccessfully() {
            Product product2 = Product.builder()
                    .id("product-id-2")
                    .name("Product 2")
                    .build();

            when(productRepositoryAdapter.getProductsByIdList(any()))
                    .thenReturn(Flux.just(product, product2));

            StepVerifier.create(productGatewayImpl.getProductsByIdList(Flux.just("product-id-1", "product-id-2")))
                    .expectNext(product)
                    .expectNext(product2)
                    .verifyComplete();
        }

        @Test
        @DisplayName("Empty Path: should throw ProductNotFoundException when no products are found")
        void getProductsByIdListNotFoundProductsThrowsProductNotFoundException() {
            when(productRepositoryAdapter.getProductsByIdList(any()))
                    .thenReturn(Flux.empty());

            StepVerifier.create(productGatewayImpl.getProductsByIdList(Flux.just("product-id-1")))
                    .expectErrorMatches(error ->
                            error instanceof ProductNotFoundException &&
                                    error.getMessage().equals(ProductConstants.MSG_PRODUCT_NOT_FOUND))
                    .verify();
        }

        @Test
        @DisplayName("Error Path: should throw GetProductsByIdListFailed when an unexpected exception occurs")
        void getProductsByIdListErrorResponseThrowsGetProductsByIdListFailed() {
            when(productRepositoryAdapter.getProductsByIdList(any()))
                    .thenReturn(Flux.error(new RuntimeException("DB connection error")));

            StepVerifier.create(productGatewayImpl.getProductsByIdList(Flux.just("product-id-1")))
                    .expectErrorMatches(error ->
                            error instanceof GetProductsByIdListFailed &&
                                    error.getMessage().equals(ProductConstants.MSG_ERROR_GET_PRODUCTS_BY_ID_LIST))
                    .verify();
        }
    }
}
