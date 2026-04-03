package co.com.organization.usecase.changenames;

import co.com.organization.model.branch.gateways.BranchRepository;
import co.com.organization.model.franchise.gateways.FranchiseRepository;
import co.com.organization.model.product.gateways.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChangeNamesUseCaseTest {
    @Mock
    private FranchiseRepository franchiseRepository;

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ChangeNamesUseCase changeNamesUseCase;

    private static final String FRANCHISE_ID = "franchise-001";
    private static final String BRANCH_ID    = "branch-001";
    private static final String PRODUCT_ID   = "product-001";

    // ------------------------------------------------------------------ //
    //  changeFranchiseName
    // ------------------------------------------------------------------ //

    @Test
    void changeFranchiseNameShouldReturnUpdatedNameResponseSuccessfully() {
        String newName = "New Franchise Name";
        String response = "The franchise name was successfully changed";
        when(franchiseRepository.changeName(FRANCHISE_ID, newName))
                .thenReturn(Mono.just(response));

        StepVerifier.create(changeNamesUseCase.changeFranchiseName(FRANCHISE_ID, newName))
                .expectNext(response)
                .verifyComplete();

        verify(franchiseRepository, times(1)).changeName(FRANCHISE_ID, newName);
    }

    @Test
    void changeFranchiseNameShouldPropagateErrorWhenRepositoryFails() {
        String newName = "New Franchise Name";
        RuntimeException error = new RuntimeException("Franchise not found");
        when(franchiseRepository.changeName(FRANCHISE_ID, newName))
                .thenReturn(Mono.error(error));

        StepVerifier.create(changeNamesUseCase.changeFranchiseName(FRANCHISE_ID, newName))
                .expectErrorMatches(ex -> ex instanceof RuntimeException
                        && ex.getMessage().equals("Franchise not found"))
                .verify();

        verify(franchiseRepository, times(1)).changeName(FRANCHISE_ID, newName);
    }

    @Test
    void changeFranchiseNameShouldCompleteEmptyWhenRepositoryReturnsEmpty() {
        String newName = "New Franchise Name";
        when(franchiseRepository.changeName(FRANCHISE_ID, newName))
                .thenReturn(Mono.empty());

        StepVerifier.create(changeNamesUseCase.changeFranchiseName(FRANCHISE_ID, newName))
                .verifyComplete();

        verify(franchiseRepository, times(1)).changeName(FRANCHISE_ID, newName);
    }

    // ------------------------------------------------------------------ //
    //  changeBranchName
    // ------------------------------------------------------------------ //

    @Test
    void changeBranchNameShouldReturnUpdatedNameResponseSuccessfully() {
        String newName = "New Branch Name";
        String response = "The branch name was successfully changed";
        when(branchRepository.changeBranchName(BRANCH_ID, newName))
                .thenReturn(Mono.just(response));

        StepVerifier.create(changeNamesUseCase.changeBranchName(BRANCH_ID, newName))
                .expectNext(response)
                .verifyComplete();

        verify(branchRepository, times(1)).changeBranchName(BRANCH_ID, newName);
    }

    @Test
    void changeBranchNameShouldPropagateErrorWhenRepositoryFails() {
        String newName = "New Branch Name";
        RuntimeException error = new RuntimeException("Branch not found");
        when(branchRepository.changeBranchName(BRANCH_ID, newName))
                .thenReturn(Mono.error(error));

        StepVerifier.create(changeNamesUseCase.changeBranchName(BRANCH_ID, newName))
                .expectErrorMatches(ex -> ex instanceof RuntimeException
                        && ex.getMessage().equals("Branch not found"))
                .verify();

        verify(branchRepository, times(1)).changeBranchName(BRANCH_ID, newName);
    }

    @Test
    void changeBranchNameShouldCompleteEmptyWhenRepositoryReturnsEmpty() {
        String newName = "New Branch Name";
        when(branchRepository.changeBranchName(BRANCH_ID, newName))
                .thenReturn(Mono.empty());

        StepVerifier.create(changeNamesUseCase.changeBranchName(BRANCH_ID, newName))
                .verifyComplete();

        verify(branchRepository, times(1)).changeBranchName(BRANCH_ID, newName);
    }

    // ------------------------------------------------------------------ //
    //  changeProductName
    // ------------------------------------------------------------------ //

    @Test
    void changeProductNameShouldReturnUpdatedNameResponseSuccessfully() {
        String newName = "New Product Name";
        String response = "Product name was successfully changed";
        when(productRepository.changeProductName(PRODUCT_ID, newName))
                .thenReturn(Mono.just(response));

        StepVerifier.create(changeNamesUseCase.changeProductName(PRODUCT_ID, newName))
                .expectNext(response)
                .verifyComplete();

        verify(productRepository, times(1)).changeProductName(PRODUCT_ID, newName);
    }

    @Test
    void changeProductNameShouldPropagateErrorWhenRepositoryFails() {
        String newName = "New Product Name";
        RuntimeException error = new RuntimeException("Product not found");
        when(productRepository.changeProductName(PRODUCT_ID, newName))
                .thenReturn(Mono.error(error));

        StepVerifier.create(changeNamesUseCase.changeProductName(PRODUCT_ID, newName))
                .expectErrorMatches(ex -> ex instanceof RuntimeException
                        && ex.getMessage().equals("Product not found"))
                .verify();

        verify(productRepository, times(1)).changeProductName(PRODUCT_ID, newName);
    }

    @Test
    void changeProductNameShouldCompleteEmptyWhenRepositoryReturnsEmpty() {
        String newName = "New Product Name";
        when(productRepository.changeProductName(PRODUCT_ID, newName))
                .thenReturn(Mono.empty());

        StepVerifier.create(changeNamesUseCase.changeProductName(PRODUCT_ID, newName))
                .verifyComplete();

        verify(productRepository, times(1)).changeProductName(PRODUCT_ID, newName);
    }
}
