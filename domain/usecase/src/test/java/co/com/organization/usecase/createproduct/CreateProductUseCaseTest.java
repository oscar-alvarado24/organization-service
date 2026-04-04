package co.com.organization.usecase.createproduct;

import co.com.organization.model.product.Product;
import co.com.organization.model.product.gateways.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateProductUseCaseTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CreateProductUseCase createProductUseCase;

    private static final String PRODUCT_ID   = "product-001";
    private static final String PRODUCT_NAME = "Product Test";

    private Product product;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(PRODUCT_ID)
                .name(PRODUCT_NAME)
                .build();
    }

    // ------------------------------------------------------------------ //
    //  Happy path
    // ------------------------------------------------------------------ //

    @Test
    void executeShouldReturnProductIdWhenProductIsCreatedSuccessfully() {
        when(productRepository.createProduct(any())).thenReturn(Mono.just(PRODUCT_ID));

        StepVerifier.create(createProductUseCase.execute(Mono.just(product)))
                .expectNext(PRODUCT_ID)
                .verifyComplete();

        verify(productRepository, times(1)).createProduct(any());
    }

    // ------------------------------------------------------------------ //
    //  Error path
    // ------------------------------------------------------------------ //

    @Test
    void executeShouldPropagateErrorWhenRepositoryFails() {
        RuntimeException error = new RuntimeException("DB connection error");

        when(productRepository.createProduct(any())).thenReturn(Mono.error(error));

        StepVerifier.create(createProductUseCase.execute(Mono.just(product)))
                .expectErrorMatches(ex -> ex instanceof RuntimeException &&
                        ex.getMessage().equals("DB connection error"))
                .verify();

        verify(productRepository, times(1)).createProduct(any());
    }

    @Test
    void executeShouldPropagateErrorWhenProductMonoHasError() {
        IllegalArgumentException error = new IllegalArgumentException("Invalid product data");

        when(productRepository.createProduct(any())).thenReturn(Mono.error(error));

        StepVerifier.create(createProductUseCase.execute(Mono.error(error)))
                .expectErrorMatches(ex -> ex instanceof IllegalArgumentException &&
                        ex.getMessage().equals("Invalid product data"))
                .verify();

        verify(productRepository, times(1)).createProduct(any());
    }

    // ------------------------------------------------------------------ //
    //  Empty path
    // ------------------------------------------------------------------ //

    @Test
    void executeShouldCompleteEmptyWhenRepositoryReturnsEmpty() {
        when(productRepository.createProduct(any())).thenReturn(Mono.empty());

        StepVerifier.create(createProductUseCase.execute(Mono.just(product)))
                .verifyComplete();

        verify(productRepository, times(1)).createProduct(any());
    }

    @Test
    void executeShouldCompleteEmptyWhenProductMonoIsEmpty() {
        when(productRepository.createProduct(any())).thenReturn(Mono.empty());

        StepVerifier.create(createProductUseCase.execute(Mono.empty()))
                .verifyComplete();

        verify(productRepository, times(1)).createProduct(any());
    }
}