package co.com.organization.dynamodb.gateway;

import co.com.organization.dynamodb.DynamoDBTemplateAdapter;
import co.com.organization.dynamodb.helper.BranchConstants;
import co.com.organization.exception.AddProductToBranchFailed;
import co.com.organization.exception.BranchAlreadyExistException;
import co.com.organization.exception.BranchNotFoundException;
import co.com.organization.exception.ChangeBranchNameFailed;
import co.com.organization.exception.ChangeStockProductFailed;
import co.com.organization.exception.CreateBranchFailed;
import co.com.organization.exception.DeleteBranchFailed;
import co.com.organization.exception.DeleteProductOfBranchFailed;
import co.com.organization.exception.GetTopProductsFailed;
import co.com.organization.exception.ProductNotFoundException;
import co.com.organization.model.branch.Branch;
import co.com.organization.model.branch.ProductBranch;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("BranchGatewayImpl")
class BranchGatewayImplTest {

    @Mock
    private DynamoDBTemplateAdapter repository;

    @InjectMocks
    private BranchGatewayImpl gateway;

    private static final String BRANCH_ID  = "branch-001";
    private static final String PRODUCT_ID = "product-001";
    private static final String NEW_NAME   = "New Branch Name";
    private static final int    NEW_STOCK  = 50;

    private static final ProductBranch PRODUCT_BRANCH = ProductBranch.builder()
            .id(PRODUCT_ID)
            .stock(NEW_STOCK)
            .build();

    private static final Branch BRANCH = Branch.builder()
            .branchId(BRANCH_ID)
            .name("Branch Test")
            .address("Address Test")
            .phone("123456789")
            .products(List.of(PRODUCT_BRANCH))
            .build();

    // ================================================================== //
    //  getBranchTopProductList
    // ================================================================== //

    @Nested
    @DisplayName("getBranchTopProductList")
    class GetBranchTopProductList {

        @Test
        @DisplayName("should return branch list when repository succeeds")
        void getTopProductListReturnTopProductBranchesSuccessfully() {
            when(repository.getTopProductList(any())).thenReturn(Mono.just(List.of(BRANCH)));

            StepVerifier.create(gateway.getBranchTopProductList(Flux.just(BRANCH_ID)))
                    .expectNext(BRANCH)
                    .verifyComplete();
        }

        @Test
        @DisplayName("should complete empty when repository returns empty list")
        void getTopProductListWithEmptyRepositoryResponseCompletesEmpty() {
            when(repository.getTopProductList(any())).thenReturn(Mono.just(List.of()));

            StepVerifier.create(gateway.getBranchTopProductList(Flux.just(BRANCH_ID)))
                    .verifyComplete();
        }

        @Test
        @DisplayName("should throw GetTopProductsFailed when repository fails")
        void getTopProductListWithErrorResponseInRepositoryThrowsGetTopProductsFailed() {
            when(repository.getTopProductList(any()))
                    .thenReturn(Mono.error(new RuntimeException("DB error")));

            StepVerifier.create(gateway.getBranchTopProductList(Flux.just(BRANCH_ID)))
                    .expectError(GetTopProductsFailed.class)
                    .verify();
        }
    }

    // ================================================================== //
    //  createBranch
    // ================================================================== //
    @Nested
    @DisplayName("createBranch")
    class CreateBranch {

        @Test
        @DisplayName("should return true when branch does not exist and is saved successfully")
        void createBranchReturnSuccessfullyMessage() {
            when(repository.validateExistence(BRANCH_ID)).thenReturn(Mono.just(false));
            when(repository.saveBranch(any())).thenReturn(Mono.just(true));

            StepVerifier.create(gateway.createBranch(Mono.just(BRANCH)))
                    .expectNext(true)
                    .verifyComplete();
        }

        @Test
        @DisplayName("should throw BranchAlreadyExistException when branch already exists")
        void createBranchWithExistingBranchIdThrowsBranchAlreadyExistException() {
            when(repository.validateExistence(BRANCH_ID)).thenReturn(Mono.just(true));

            StepVerifier.create(gateway.createBranch(Mono.just(BRANCH)))
                    .expectError(BranchAlreadyExistException.class)
                    .verify();
        }

        @Test
        @DisplayName("should throw CreateBranchFailed when repository fails on save")
        void createBranchWithErrorResponseInRepositoryThrowsCreateBranchFailed() {
            when(repository.validateExistence(BRANCH_ID)).thenReturn(Mono.just(false));
            when(repository.saveBranch(any()))
                    .thenReturn(Mono.error(new RuntimeException("DB error")));

            StepVerifier.create(gateway.createBranch(Mono.just(BRANCH)))
                    .expectError(CreateBranchFailed.class)
                    .verify();
        }

        @Test
        @DisplayName("should throw CreateBranchFailed when repository fails on validate")
        void createBranchWithErrorOnValidationThrowsCreateBranchFailed() {
            when(repository.validateExistence(BRANCH_ID))
                    .thenReturn(Mono.error(new RuntimeException("DB error")));

            StepVerifier.create(gateway.createBranch(Mono.just(BRANCH)))
                    .expectError(CreateBranchFailed.class)
                    .verify();
        }
    }

    // ================================================================== //
    //  addProductToBranch
    // ================================================================== //

    @Nested
    @DisplayName("addProductToBranch")
    class AddProductToBranch {

        @Test
        @DisplayName("should return branchId when repository succeeds")
        void addProductToBranchSuccessfullyReturn() {
            when(repository.addProductToBranch(BRANCH_ID, PRODUCT_BRANCH))
                    .thenReturn(Mono.just(BRANCH_ID));

            StepVerifier.create(gateway.addProductToBranch(BRANCH_ID, PRODUCT_BRANCH))
                    .expectNext(BRANCH_ID)
                    .verifyComplete();
        }

        @Test
        @DisplayName("should complete empty when repository returns empty")
        void addProductToBranchWithEmptyRepositoryResponseCompletesEmpty() {
            when(repository.addProductToBranch(BRANCH_ID, PRODUCT_BRANCH))
                    .thenReturn(Mono.empty());

            StepVerifier.create(gateway.addProductToBranch(BRANCH_ID, PRODUCT_BRANCH))
                    .verifyComplete();
        }

        @Test
        @DisplayName("should throw AddProductToBranchFailed when repository fails")
        void addProductToBranchWithErrorResponseInRepositoryReturnAddProductToBranchFailed() {
            when(repository.addProductToBranch(BRANCH_ID, PRODUCT_BRANCH))
                    .thenReturn(Mono.error(new RuntimeException("DB error")));

            StepVerifier.create(gateway.addProductToBranch(BRANCH_ID, PRODUCT_BRANCH))
                    .expectError(AddProductToBranchFailed.class)
                    .verify();
        }
    }

    // ================================================================== //
    //  deleteProductOfBranch
    // ================================================================== //

    @Nested
    @DisplayName("deleteProductOfBranch")
    class DeleteProductOfBranch {

        @Test
        @DisplayName("should return success message when repository succeeds")
        void deleteProductOfBranchSuccessfullyReturnMessageSuccessfully() {
            when(repository.deleteProductOfBranch(BRANCH_ID, PRODUCT_ID))
                    .thenReturn(Mono.just(BranchConstants.MSG_PRODUCT_DELETED_SUCCESSFULLY));

            StepVerifier.create(gateway.deleteProductOfBranch(BRANCH_ID, PRODUCT_ID))
                    .expectNext(BranchConstants.MSG_PRODUCT_DELETED_SUCCESSFULLY)
                    .verifyComplete();
        }

        @Test
        @DisplayName("should complete empty when repository returns empty")
        void deleteProductOfBranchWithEmptyRepositoryResponseCompletesEmpty() {
            when(repository.deleteProductOfBranch(BRANCH_ID, PRODUCT_ID))
                    .thenReturn(Mono.empty());

            StepVerifier.create(gateway.deleteProductOfBranch(BRANCH_ID, PRODUCT_ID))
                    .verifyComplete();
        }

        @Test
        @DisplayName("should throw DeleteProductOfBranchFailed when repository fails")
        void deleteProductOfBranchWithErrorResponseInRepositoryThrowsDeleteProductOfBranchFailed() {
            when(repository.deleteProductOfBranch(BRANCH_ID, PRODUCT_ID))
                    .thenReturn(Mono.error(new RuntimeException("DB error")));

            StepVerifier.create(gateway.deleteProductOfBranch(BRANCH_ID, PRODUCT_ID))
                    .expectError(DeleteProductOfBranchFailed.class)
                    .verify();
        }
    }

    // ================================================================== //
    //  changeProductStock
    // ================================================================== //

    @Nested
    @DisplayName("changeProductStock")
    class ChangeProductStock {

        @Test
        @DisplayName("should return success message when repository succeeds")
        void changeProductStockSuccessfullyReturnProductId() {
            when(repository.changeProductStock(BRANCH_ID, PRODUCT_ID, NEW_STOCK))
                    .thenReturn(Mono.just(BranchConstants.MSG_CHANGE_PRODUCT_STOCK_SUCCESSFULLY));

            StepVerifier.create(gateway.changeProductStock(BRANCH_ID, PRODUCT_ID, NEW_STOCK))
                    .expectNext(BranchConstants.MSG_CHANGE_PRODUCT_STOCK_SUCCESSFULLY)
                    .verifyComplete();
        }

        @Test
        @DisplayName("should throw ProductNotFoundException when product is not found")
        void changeProductStockWithEmptyRepositoryResponseThrowsProductNotFoundException() {
            when(repository.changeProductStock(BRANCH_ID, PRODUCT_ID, NEW_STOCK))
                    .thenReturn(Mono.empty());

            StepVerifier.create(gateway.changeProductStock(BRANCH_ID, PRODUCT_ID, NEW_STOCK))
                    .expectError(ProductNotFoundException.class)
                    .verify();
        }

        @Test
        @DisplayName("should throw ChangeStockProductFailed when repository fails")
        void changeProductStockWithErrorResponseInRepositoryThrowsChangeStockProductFailed() {
            when(repository.changeProductStock(BRANCH_ID, PRODUCT_ID, NEW_STOCK))
                    .thenReturn(Mono.error(new RuntimeException("DB error")));

            StepVerifier.create(gateway.changeProductStock(BRANCH_ID, PRODUCT_ID, NEW_STOCK))
                    .expectError(ChangeStockProductFailed.class)
                    .verify();
        }
    }

    // ================================================================== //
    //  changeBranchName
    // ================================================================== //

    @Nested
    @DisplayName("changeBranchName")
    class ChangeBranchName {

        @Test
        @DisplayName("should return successfully message when repository succeeds")
        void changeBranchNameSuccessfullyReturnMessageSuccessfully() {
            when(repository.changeBranchName(BRANCH_ID, NEW_NAME))
                    .thenReturn(Mono.just(BranchConstants.MSG_CHANGE_BRANCH_NAME_SUCCESSFULLY));

            StepVerifier.create(gateway.changeBranchName(BRANCH_ID, NEW_NAME))
                    .expectNext(BranchConstants.MSG_CHANGE_BRANCH_NAME_SUCCESSFULLY)
                    .verifyComplete();
        }

        @Test
        @DisplayName("should throw BranchNotFoundException when branch is not found")
        void changeBranchNameWithEmptyRepositoryResponseThrowsBranchNotFoundException() {
            when(repository.changeBranchName(BRANCH_ID, NEW_NAME))
                    .thenReturn(Mono.empty());

            StepVerifier.create(gateway.changeBranchName(BRANCH_ID, NEW_NAME))
                    .expectError(BranchNotFoundException.class)
                    .verify();
        }

        @Test
        @DisplayName("should throw ChangeBranchNameFailed when repository fails")
        void changeBranchNameWithErrorResponseInRepositoryThrowsChangeBranchNameFailed() {
            when(repository.changeBranchName(BRANCH_ID, NEW_NAME))
                    .thenReturn(Mono.error(new RuntimeException("DB error")));

            StepVerifier.create(gateway.changeBranchName(BRANCH_ID, NEW_NAME))
                    .expectError(ChangeBranchNameFailed.class)
                    .verify();
        }
    }

    // ================================================================== //
    //  delete
    // ================================================================== //

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("should complete when repository succeeds")
        void deleteBranchSuccessfullyCompletes() {
            when(repository.deleteBranch(BRANCH_ID)).thenReturn(Mono.empty());

            StepVerifier.create(gateway.delete(BRANCH_ID))
                    .verifyComplete();
        }

        @Test
        @DisplayName("should throw DeleteBranchFailed when repository fails")
        void deleteBranchWithErrorResponseInRepositoryThrowsDeleteBranchFailed() {
            when(repository.deleteBranch(BRANCH_ID))
                    .thenReturn(Mono.error(new RuntimeException("DB error")));

            StepVerifier.create(gateway.delete(BRANCH_ID))
                    .expectError(DeleteBranchFailed.class)
                    .verify();
        }
    }
}