package co.com.organization.model.product.gateways;

import co.com.organization.model.product.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductRepository {
    Mono<String> createProduct(Mono<Product> product);
    Mono<String> changeProductName(String productId, String newName);
    Mono<Void> validateExistence(String productId);
    Flux<Product> getProductsForIdList(Flux<String> idList);
}
