package co.com.organization.usecase.higheststockproductperbranch;

import co.com.organization.model.branch.Branch;
import co.com.organization.model.branch.ProductBranch;
import co.com.organization.model.branch.gateways.BranchRepository;
import co.com.organization.model.franchise.Franchise;
import co.com.organization.model.franchise.gateways.FranchiseRepository;
import co.com.organization.model.product.Product;
import co.com.organization.model.product.gateways.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HighestStockProductPerBranchUseCaseTest {

    @Mock
    private FranchiseRepository franchiseRepository;

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private HighestStockProductPerBranchUseCase useCase;

    private static final String FRANCHISE_ID = "franchise-001";
    private static final String BRANCH_ID_1  = "branch-001";
    private static final String BRANCH_ID_2  = "branch-002";
    private static final String PRODUCT_ID_1 = "product-001";
    private static final String PRODUCT_ID_2 = "product-002";

    private Franchise franchise;

    @BeforeEach
    void setUp() {
        franchise = Franchise.builder()
                .id(FRANCHISE_ID)
                .branchIds(List.of(BRANCH_ID_1, BRANCH_ID_2))
                .build();
    }

    // ------------------------------------------------------------------ //
    //  Happy path
    // ------------------------------------------------------------------ //

    @Test
    void executeShouldReturnTopProductPerBranchWhenAllDataIsPresent() {
        Branch branch1 = Branch.builder()
                .id(BRANCH_ID_1)
                .name("Branch One")
                .products(List.of(ProductBranch.builder().id(PRODUCT_ID_1).build()))
                .build();

        Branch branch2 = Branch.builder()
                .id(BRANCH_ID_2)
                .name("Branch Two")
                .products(List.of(ProductBranch.builder().id(PRODUCT_ID_2).build()))
                .build();

        Product product1 = Product.builder().id(PRODUCT_ID_1).name("Product One").build();
        Product product2 = Product.builder().id(PRODUCT_ID_2).name("Product Two").build();

        when(franchiseRepository.getFranchiseById(FRANCHISE_ID)).thenReturn(Mono.just(franchise));
        when(branchRepository.getBranchTopProductList(any())).thenReturn(Flux.just(branch1, branch2));
        when(productRepository.getProductsByIdList(any())).thenReturn(Flux.just(product1, product2));

        StepVerifier.create(useCase.execute(FRANCHISE_ID))
                .expectNextMatches(btp ->
                        btp.getBranchName().equals("Branch One") &&
                                btp.getProductId().equals(PRODUCT_ID_1) &&
                                btp.getProductName().equals("Product One"))
                .expectNextMatches(btp ->
                        btp.getBranchName().equals("Branch Two") &&
                                btp.getProductId().equals(PRODUCT_ID_2) &&
                                btp.getProductName().equals("Product Two"))
                .verifyComplete();

        verify(franchiseRepository, times(1)).getFranchiseById(FRANCHISE_ID);
        verify(branchRepository, times(1)).getBranchTopProductList(any());
        verify(productRepository, times(1)).getProductsByIdList(any());
    }

    @Test
    void executeShouldReturnSingleResultWhenMultipleBranchesShareSameTopProduct() {
        Branch branch1 = Branch.builder()
                .id(BRANCH_ID_1)
                .name("Branch One")
                .products(List.of(ProductBranch.builder().id(PRODUCT_ID_1).build()))
                .build();

        Branch branch2 = Branch.builder()
                .id(BRANCH_ID_2)
                .name("Branch Two")
                .products(List.of(ProductBranch.builder().id(PRODUCT_ID_1).build()))
                .build();

        Product product1 = Product.builder().id(PRODUCT_ID_1).name("Product One").build();

        when(franchiseRepository.getFranchiseById(FRANCHISE_ID)).thenReturn(Mono.just(franchise));
        when(branchRepository.getBranchTopProductList(any())).thenReturn(Flux.just(branch1, branch2));
        when(productRepository.getProductsByIdList(any())).thenReturn(Flux.just(product1));

        StepVerifier.create(useCase.execute(FRANCHISE_ID))
                .expectNextCount(2)
                .verifyComplete();

        verify(productRepository, times(1)).getProductsByIdList(any());
    }

    // ------------------------------------------------------------------ //
    //  Empty path
    // ------------------------------------------------------------------ //

    @Test
    void executeShouldCompleteEmptyWhenFranchiseHasNoBranches() {
        Franchise franchiseNoBranches = Franchise.builder()
                .id(FRANCHISE_ID)
                .branchIds(List.of())
                .build();

        when(franchiseRepository.getFranchiseById(FRANCHISE_ID)).thenReturn(Mono.just(franchiseNoBranches));
        when(branchRepository.getBranchTopProductList(any())).thenReturn(Flux.empty());

        StepVerifier.create(useCase.execute(FRANCHISE_ID))
                .verifyComplete();

        verify(franchiseRepository, times(1)).getFranchiseById(FRANCHISE_ID);
        verify(branchRepository, times(1)).getBranchTopProductList(any());
        verify(productRepository, never()).getProductsByIdList(any());
    }

    @Test
    void executeShouldCompleteEmptyWhenAllBranchesHaveNoProducts() {
        Branch branch1 = Branch.builder()
                .id(BRANCH_ID_1)
                .name("Branch One")
                .products(List.of())
                .build();

        Branch branch2 = Branch.builder()
                .id(BRANCH_ID_2)
                .name("Branch Two")
                .products(null)
                .build();

        when(franchiseRepository.getFranchiseById(FRANCHISE_ID)).thenReturn(Mono.just(franchise));
        when(branchRepository.getBranchTopProductList(any())).thenReturn(Flux.just(branch1, branch2));

        StepVerifier.create(useCase.execute(FRANCHISE_ID))
                .verifyComplete();

        verify(branchRepository, times(1)).getBranchTopProductList(any());
        verify(productRepository, never()).getProductsByIdList(any());
    }

    @Test
    void executeShouldCompleteEmptyWhenFranchiseNotFound() {
        when(franchiseRepository.getFranchiseById(FRANCHISE_ID)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(FRANCHISE_ID))
                .verifyComplete();

        verify(franchiseRepository, times(1)).getFranchiseById(FRANCHISE_ID);
        verify(branchRepository, never()).getBranchTopProductList(any());
        verify(productRepository, never()).getProductsByIdList(any());
    }

    // ------------------------------------------------------------------ //
    //  Error path
    // ------------------------------------------------------------------ //

    @Test
    void executeShouldPropagateErrorWhenFranchiseRepositoryFails() {
        RuntimeException error = new RuntimeException("Franchise DB error");

        when(franchiseRepository.getFranchiseById(FRANCHISE_ID)).thenReturn(Mono.error(error));

        StepVerifier.create(useCase.execute(FRANCHISE_ID))
                .expectErrorMatches(ex -> ex instanceof RuntimeException &&
                        ex.getMessage().equals("Franchise DB error"))
                .verify();

        verify(franchiseRepository, times(1)).getFranchiseById(FRANCHISE_ID);
        verify(branchRepository, never()).getBranchTopProductList(any());
        verify(productRepository, never()).getProductsByIdList(any());
    }

    @Test
    void executeShouldPropagateErrorWhenBranchRepositoryFails() {
        RuntimeException error = new RuntimeException("Branch DB error");

        when(franchiseRepository.getFranchiseById(FRANCHISE_ID)).thenReturn(Mono.just(franchise));
        when(branchRepository.getBranchTopProductList(any())).thenReturn(Flux.error(error));

        StepVerifier.create(useCase.execute(FRANCHISE_ID))
                .expectErrorMatches(ex -> ex instanceof RuntimeException &&
                        ex.getMessage().equals("Branch DB error"))
                .verify();

        verify(branchRepository, times(1)).getBranchTopProductList(any());
        verify(productRepository, never()).getProductsByIdList(any());
    }

    @Test
    void executeShouldPropagateErrorWhenProductRepositoryFails() {
        Branch branch1 = Branch.builder()
                .id(BRANCH_ID_1)
                .name("Branch One")
                .products(List.of(ProductBranch.builder().id(PRODUCT_ID_1).build()))
                .build();

        RuntimeException error = new RuntimeException("Product DB error");

        when(franchiseRepository.getFranchiseById(FRANCHISE_ID)).thenReturn(Mono.just(franchise));
        when(branchRepository.getBranchTopProductList(any())).thenReturn(Flux.just(branch1));
        when(productRepository.getProductsByIdList(any())).thenReturn(Flux.error(error));

        StepVerifier.create(useCase.execute(FRANCHISE_ID))
                .expectErrorMatches(ex -> ex instanceof RuntimeException &&
                        ex.getMessage().equals("Product DB error"))
                .verify();

        verify(productRepository, times(1)).getProductsByIdList(any());
    }
}