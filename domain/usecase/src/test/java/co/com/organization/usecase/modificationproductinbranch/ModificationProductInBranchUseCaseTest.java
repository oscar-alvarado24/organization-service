package co.com.organization.usecase.modificationproductinbranch;

import co.com.organization.model.branch.ProductBranch;
import co.com.organization.model.branch.gateways.BranchRepository;
import co.com.organization.model.product.gateways.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ModificationProductInBranchUseCaseTest {

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ModificationProductInBranchUseCase useCase;

    private static final String BRANCH_ID  = "branch-001";
    private static final String PRODUCT_ID = "product-001";
    private static final int    NEW_STOCK  = 50;
    private static final ProductBranch PRODUCT_BRANCH = new ProductBranch(PRODUCT_ID, 50);
    private static final Mono<ProductBranch> PRODUCT_BRANCH_MONO   = Mono.just(PRODUCT_BRANCH);

    // ================================================================== //
    //  addProductToBranch
    // ================================================================== //

    @Test
    void addProductToBranchShouldReturnBranchIdWhenProductExistsAndIsAdded() {
        when(productRepository.validateExistence(PRODUCT_ID)).thenReturn(Mono.empty());
        when(branchRepository.addProductToBranch(BRANCH_ID, PRODUCT_BRANCH)).thenReturn(Mono.just(BRANCH_ID));

        StepVerifier.create(useCase.addProductToBranch(BRANCH_ID, PRODUCT_BRANCH_MONO))
                .expectNext(BRANCH_ID)
                .verifyComplete();

        verify(productRepository, times(1)).validateExistence(PRODUCT_ID);
        verify(branchRepository, times(1)).addProductToBranch(BRANCH_ID, PRODUCT_BRANCH);
    }

    @Test
    void addProductToBranchShouldPropagateErrorWhenProductValidationFails() {
        RuntimeException error = new RuntimeException("Product not found");

        when(productRepository.validateExistence(PRODUCT_ID)).thenReturn(Mono.error(error));

        StepVerifier.create(useCase.addProductToBranch(BRANCH_ID, PRODUCT_BRANCH_MONO))
                .expectErrorMatches(ex -> ex instanceof RuntimeException &&
                        ex.getMessage().equals("Product not found"))
                .verify();

        verify(productRepository, times(1)).validateExistence(PRODUCT_ID);
        verify(branchRepository, never()).addProductToBranch(any(), any());
    }

    @Test
    void addProductToBranchShouldPropagateErrorWhenAddProductFails() {
        RuntimeException error = new RuntimeException("Branch DB error");

        when(productRepository.validateExistence(PRODUCT_ID)).thenReturn(Mono.empty());
        when(branchRepository.addProductToBranch(BRANCH_ID, PRODUCT_BRANCH)).thenReturn(Mono.error(error));

        StepVerifier.create(useCase.addProductToBranch(BRANCH_ID, PRODUCT_BRANCH_MONO))
                .expectErrorMatches(ex -> ex instanceof RuntimeException &&
                        ex.getMessage().equals("Branch DB error"))
                .verify();

        verify(productRepository, times(1)).validateExistence(PRODUCT_ID);
        verify(branchRepository, times(1)).addProductToBranch(BRANCH_ID, PRODUCT_BRANCH);
    }

    @Test
    void addProductToBranchShouldCompleteEmptyWhenAddProductReturnsEmpty() {
        when(productRepository.validateExistence(PRODUCT_ID)).thenReturn(Mono.empty());
        when(branchRepository.addProductToBranch(BRANCH_ID, PRODUCT_BRANCH)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.addProductToBranch(BRANCH_ID, PRODUCT_BRANCH_MONO))
                .verifyComplete();

        verify(branchRepository, times(1)).addProductToBranch(BRANCH_ID, PRODUCT_BRANCH);
    }

    // ================================================================== //
    //  deleteProductToBranch
    // ================================================================== //

    @Test
    void deleteProductToBranchShouldReturnProductIdWhenDeleteSucceeds() {
        when(branchRepository.deleteProductToBranch(BRANCH_ID, PRODUCT_ID)).thenReturn(Mono.just(PRODUCT_ID));

        StepVerifier.create(useCase.deleteProductToBranch(BRANCH_ID, PRODUCT_ID))
                .expectNext(PRODUCT_ID)
                .verifyComplete();

        verify(branchRepository, times(1)).deleteProductToBranch(BRANCH_ID, PRODUCT_ID);
        verify(productRepository, never()).validateExistence(any());
    }

    @Test
    void deleteProductToBranchShouldPropagateErrorWhenRepositoryFails() {
        RuntimeException error = new RuntimeException("Delete failed");

        when(branchRepository.deleteProductToBranch(BRANCH_ID, PRODUCT_ID)).thenReturn(Mono.error(error));

        StepVerifier.create(useCase.deleteProductToBranch(BRANCH_ID, PRODUCT_ID))
                .expectErrorMatches(ex -> ex instanceof RuntimeException &&
                        ex.getMessage().equals("Delete failed"))
                .verify();

        verify(branchRepository, times(1)).deleteProductToBranch(BRANCH_ID, PRODUCT_ID);
    }

    @Test
    void deleteProductToBranchShouldCompleteEmptyWhenRepositoryReturnsEmpty() {
        when(branchRepository.deleteProductToBranch(BRANCH_ID, PRODUCT_ID)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.deleteProductToBranch(BRANCH_ID, PRODUCT_ID))
                .verifyComplete();

        verify(branchRepository, times(1)).deleteProductToBranch(BRANCH_ID, PRODUCT_ID);
    }

    // ================================================================== //
    //  changeStockProduct
    // ================================================================== //

    @Test
    void changeStockProductShouldReturnProductIdWhenStockIsUpdated() {
        when(branchRepository.changeProductStock(BRANCH_ID, PRODUCT_ID, NEW_STOCK)).thenReturn(Mono.just(PRODUCT_ID));

        StepVerifier.create(useCase.changeStockProduct(BRANCH_ID, PRODUCT_ID, NEW_STOCK))
                .expectNext(PRODUCT_ID)
                .verifyComplete();

        verify(branchRepository, times(1)).changeProductStock(BRANCH_ID, PRODUCT_ID, NEW_STOCK);
        verify(productRepository, never()).validateExistence(any());
    }

    @Test
    void changeStockProductShouldPropagateErrorWhenRepositoryFails() {
        RuntimeException error = new RuntimeException("Stock update failed");

        when(branchRepository.changeProductStock(BRANCH_ID, PRODUCT_ID, NEW_STOCK)).thenReturn(Mono.error(error));

        StepVerifier.create(useCase.changeStockProduct(BRANCH_ID, PRODUCT_ID, NEW_STOCK))
                .expectErrorMatches(ex -> ex instanceof RuntimeException &&
                        ex.getMessage().equals("Stock update failed"))
                .verify();

        verify(branchRepository, times(1)).changeProductStock(BRANCH_ID, PRODUCT_ID, NEW_STOCK);
    }

    @Test
    void changeStockProductShouldCompleteEmptyWhenRepositoryReturnsEmpty() {
        when(branchRepository.changeProductStock(BRANCH_ID, PRODUCT_ID, NEW_STOCK)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.changeStockProduct(BRANCH_ID, PRODUCT_ID, NEW_STOCK))
                .verifyComplete();

        verify(branchRepository, times(1)).changeProductStock(BRANCH_ID, PRODUCT_ID, NEW_STOCK);
    }
}