package co.com.organization.usecase.modificationproductinbranch;

import co.com.organization.model.branch.gateways.BranchRepository;
import co.com.organization.model.product.gateways.ProductRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ModificationProductInBranchUseCase {
    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;
    Mono<String> addProductToBranch(String branchId, String productId){
        return productRepository.validateExistence(productId)
                .then(branchRepository.addProductToBranch(branchId,productId));
    }

    Mono<String> deleteProductToBranch(String branchId, String productId){
        return branchRepository.deleteProductToBranch(branchId, productId);
    }

    Mono<String> changeStockProduct (String branchId, String productId, int newStock){
        return branchRepository.changeProductStock(branchId, productId, newStock);
    }
}
