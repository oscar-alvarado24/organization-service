package co.com.organization.model.product.gateways;

import co.com.organization.model.product.Product;
import reactor.core.publisher.Mono;

public interface ProductRepository {
    Mono<String> createProduct(Mono<Product> product);
    Mono<Product> getProductById(String productId);
    Mono<String> changeProductName(String productId, String newName);
}
