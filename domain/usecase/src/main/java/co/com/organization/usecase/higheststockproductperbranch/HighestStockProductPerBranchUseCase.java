package co.com.organization.usecase.higheststockproductperbranch;

import co.com.organization.model.branch.BranchTopProduct;
import co.com.organization.model.branch.gateways.BranchRepository;
import co.com.organization.model.franchise.gateways.FranchiseRepository;
import co.com.organization.model.product.gateways.ProductRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class HighestStockProductPerBranchUseCase {
    private final FranchiseRepository franchiseRepository;
    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;

    public Flux<BranchTopProduct> execute(String franchiseId) {
        return franchiseRepository.getFranchiseById(franchiseId)
                .flatMapMany(franchise ->
                        branchRepository.getBranchTopProductList(Flux.fromIterable(franchise.getBranchIds()))
                )
                .filter(branch -> branch.getProducts() != null && !branch.getProducts().isEmpty())
                .map(branch -> BranchTopProduct.builder()
                        .branchName(branch.getName())
                        .productId(branch.getProducts().getFirst().getId())
                        .build()
                )
                .collectList()
                .filter(list -> !list.isEmpty())
                .flatMapMany(branchTopProducts -> {
                    Map<String, List<BranchTopProduct>> productBranchMap = branchTopProducts.stream()
                            .collect(Collectors.groupingBy(BranchTopProduct::getProductId));

                    return productRepository.getProductsByIdList(Flux.fromIterable(productBranchMap.keySet()))
                            .flatMap(product ->
                                    Flux.fromIterable(
                                            productBranchMap.get(product.getId()).stream()
                                                    .map(btp -> btp.toBuilder()
                                                            .productName(product.getName())
                                                            .build()
                                                    )
                                                    .toList()
                                    )
                            );
                });
    }
}
