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

    @SuppressWarnings("PointlessBooleanExpression")
    @Override
    public Mono<Boolean> createBranch(Mono<Branch> branch) {

        return branch.flatMap(br ->repository.validateExistence(br.getBranchId())
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        log.error(BranchConstants.LOG_ERROR_BRANCH_ALREADY_EXISTS, br.getBranchId());
                        return Mono.error(new BranchAlreadyExistException(BranchConstants.MSG_ERROR_BRANCH_ALREADY_EXISTS));
                    }
                    return repository.saveBranch(Mono.just(br));

                }))
                .onErrorResume(error -> {
                    if (error instanceof BranchAlreadyExistException) return Mono.error(error);
                    log.error(BranchConstants.LOG_ERROR_CREATING_BRANCH, error);
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
                .switchIfEmpty(Mono.error(new ProductNotFoundException(BranchConstants.MSG_ERROR_PRODUCT_NOT_FOUND)))
                .onErrorResume(error ->{
                    if (error instanceof ProductNotFoundException) return Mono.error(error);
                    log.error(BranchConstants.LOG_ERROR_CHANGE_STOCK_PRODUCT, productId, branchId, error);
                    return Mono.error(new ChangeStockProductFailed(BranchConstants.MSG_ERROR_CHANGE_STOCK_PRODUCT));
                });
    }

    @Override
    public Mono<String> changeBranchName(String branchId, String newName) {
        return repository.changeBranchName(branchId, newName)
                .switchIfEmpty(Mono.error(new BranchNotFoundException(BranchConstants.MSG_ERROR_BRANCH_NOT_FOUND)))
                .onErrorResume(error -> {
                    if (error instanceof BranchNotFoundException) return Mono.error(error);
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
