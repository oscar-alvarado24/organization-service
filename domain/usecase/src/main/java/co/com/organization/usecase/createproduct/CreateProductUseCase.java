package co.com.organization.usecase.createproduct;

import co.com.organization.model.product.Product;
import co.com.organization.model.product.gateways.ProductRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class CreateProductUseCase {
    private final ProductRepository productRepository;
    public Mono<String> execute(Mono<Product> product){
        return productRepository.createProduct(product);
    }
}
