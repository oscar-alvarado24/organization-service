package co.com.organization.model.branch.gateways;

import co.com.organization.model.branch.Branch;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BranchRepository {
    Flux<Branch> getBranchTopProductList(Flux<String> branchIdList);
    Mono<Branch> getBranchById(String branchId);
    Mono<Boolean> createBranch(Mono<Branch> branch);
    Mono<String> addProductToBranch(String branchId, String productId);
    Mono<String> deleteProductToBranch(String branchId, String productId);
    Mono<String> changeProductStock(String branchId, String productId, int stock);
    Mono<String> changeBranchName(String branchId, String newName);
    Mono<Boolean> delete(String branchId);
}
