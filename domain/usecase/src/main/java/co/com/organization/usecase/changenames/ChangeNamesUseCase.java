package co.com.organization.usecase.changenames;

import co.com.organization.model.branch.gateways.BranchRepository;
import co.com.organization.model.franchise.gateways.FranchiseRepository;
import co.com.organization.model.product.gateways.ProductRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ChangeNamesUseCase {
    private final FranchiseRepository franchiseRepository;
    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;
    public Mono<String> changeFranchiseName(String franchiseId, String newFranchiseName) {
        return franchiseRepository.changeName(franchiseId, newFranchiseName);
    }

    public Mono<String> changeBranchName(String branchId, String newBranchName) {
        return branchRepository.changeBranchName(branchId, newBranchName);
    }

    public Mono<String> changeProductName(String productId, String newProductName) {
        return productRepository.changeProductName(productId, newProductName);
    }
}
