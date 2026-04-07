package co.com.organization.dynamodb.gateway;

import co.com.organization.dynamodb.DynamoDBTemplateAdapter;
import co.com.organization.dynamodb.helper.BranchConstants;
import co.com.organization.exception.AddProductToBranchFailed;
import co.com.organization.exception.ChangeBranchNameFailed;
import co.com.organization.exception.ChangeStockProductFailed;
import co.com.organization.exception.CreateBranchFailed;
import co.com.organization.exception.DeleteBranchFailed;
import co.com.organization.exception.DeleteProductOfBranchFailed;
import co.com.organization.exception.GetTopProductsFailed;
import co.com.organization.model.branch.Branch;
import co.com.organization.model.branch.ProductBranch;
import co.com.organization.model.branch.gateways.BranchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Slf4j
public class BranchGatewayImpl implements BranchRepository {
    private final DynamoDBTemplateAdapter repository;
    @Override
    public Flux<Branch> getBranchTopProductList(Flux<String> branchIdList) {
        return branchIdList
                .collectList()
                .flatMap(repository::getTopProductList)
                .flatMapMany(Flux::fromIterable)
                .onErrorResume(error->{
                    log.error(BranchConstants.LOG_ERROR_GETTING_TOP_PRODUCTS,error);
                    return Flux.error(new GetTopProductsFailed(BranchConstants.MSG_ERROR_GETTING_TOP_PRODUCTS));
                });
    }

    @Override
    public Mono<Boolean> createBranch(Mono<Branch> branch) {
        return repository.saveBranch(branch)
                .onErrorResume(error->{
                    log.error(BranchConstants.LOG_ERROR_CREATING_BRANCH,error);
                    return Mono.error(new CreateBranchFailed(BranchConstants.MSG_ERROR_CREATING_BRANCH));
                });
    }

    @Override
    public Mono<String> addProductToBranch(String branchId, ProductBranch product) {
        return repository.addProductToBranch(branchId, product)
                .onErrorResume(error ->{
                    log.error(BranchConstants.LOG_ERROR_ADDING_PRODUCT_TO_BRANCH, product.getId(), error);
                    return Mono.error(new AddProductToBranchFailed(BranchConstants.MSG_ERROR_ADDING_PRODUCT_TO_BRANCH));
                });
    }

    public Mono<String> deleteProductOfBranch(String branchId, String productId) {
        return repository.deleteProductOfBranch(branchId, productId)
                .onErrorResume(error -> {
                    log.error(BranchConstants.LOG_ERROR_DELETE_PRODUCT_OF_BRANCH, productId,branchId, error);
                    return Mono.error(new DeleteProductOfBranchFailed(BranchConstants.MSG_ERROR_DELETE_PRODUCT_OF_BRANCH));
                });
    }

    @Override
    public Mono<String> changeProductStock(String branchId, String productId, int stock) {
        return repository.changeProductStock(branchId, productId, stock)
                .onErrorResume(error ->{
                    log.error(BranchConstants.LOG_ERROR_CHANGE_STOCK_PRODUCT, productId, branchId, error);
                    return Mono.error(new ChangeStockProductFailed(BranchConstants.MSG_ERROR_CHANGE_STOCK_PRODUCT));
                });
    }

    @Override
    public Mono<String> changeBranchName(String branchId, String newName) {
        return repository.changeBranchName(branchId, newName)
                .onErrorResume(error -> {
                    log.error(BranchConstants.LOG_ERROR_CHANGE_BRANCH_NAME, branchId, error);
                    return Mono.error(new ChangeBranchNameFailed(BranchConstants.MSG_ERROR_CHANGE_BRANCH_NAME));
                });
    }

    @Override
    public Mono<Void> delete(String branchId) {
        return repository.deleteBranch(branchId).onErrorResume(error -> {
            log.error(BranchConstants.LOG_ERROR_DELETE_BRANCH, branchId, error);
            return Mono.error(new DeleteBranchFailed(BranchConstants.MSG_ERROR_DELETE_BRANCH));
        });
    }
}
