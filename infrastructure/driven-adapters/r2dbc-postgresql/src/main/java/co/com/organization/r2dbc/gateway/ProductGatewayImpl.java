package co.com.organization.r2dbc.gateway;

import co.com.organization.exception.ChangeProductNameFailed;
import co.com.organization.exception.CreateProductFailed;
import co.com.organization.exception.GetProductsByIdListFailed;
import co.com.organization.exception.ProductNotFoundException;
import co.com.organization.exception.ValidateExistenceFailed;
import co.com.organization.model.product.Product;
import co.com.organization.model.product.gateways.ProductRepository;
import co.com.organization.r2dbc.ProductRepositoryAdapter;
import co.com.organization.r2dbc.entity.ProductEntity;
import co.com.organization.r2dbc.helper.ProductConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Slf4j
public class ProductGatewayImpl implements ProductRepository {
    private final ProductRepositoryAdapter productRepositoryAdapter;
    @Override
    public Mono<String> createProduct(Mono<Product> product) {
        return productRepositoryAdapter.save(product)
                .switchIfEmpty(Mono.error(new CreateProductFailed(ProductConstants.MSG_ERROR_SAVED_PRODUCT)))
                .onErrorResume(error->{
                    if (error instanceof CreateProductFailed) {
                        log.error(error.getMessage());
                        return Mono.error(error);
                    }
                    log.error(ProductConstants.LOG_ERROR_CREATING_PRODUCT,error);
                    return Mono.error(new CreateProductFailed(ProductConstants.MSG_ERROR_SAVED_PRODUCT));
                });
    }

    @Override
    public Mono<String> changeProductName(String productId, String newName) {
        return productRepositoryAdapter.getProductById(productId)
                .switchIfEmpty(Mono.error(new ProductNotFoundException(ProductConstants.MSG_PRODUCT_NOT_FOUND)))
                .flatMap(product -> {
                    ProductEntity productUpdated = product.toBuilder().name(newName).build();
                    return productRepositoryAdapter.update(Mono.just(productUpdated));
                })
                .onErrorResume(error ->{
                    if (error instanceof ProductNotFoundException) {
                        log.error(error.getMessage());
                        return Mono.error(error);
                    }
                    log.error(ProductConstants.LOG_ERROR_CHANGE_PRODUCT_NAME , error);
                    return Mono.error(new ChangeProductNameFailed(ProductConstants.MSG_ERROR_CHANGE_PRODUCT_NAME));
                });
    }

    @Override
    public Mono<Void> validateExistence(String productId) {
        return productRepositoryAdapter.validateExistence(productId)
                .flatMap(exists -> {
                    if (!exists) {
                        log.error(ProductConstants.LOG_ERROR_VALIDATING_PRODUCT_EXISTENCE + ProductConstants.MSG_PRODUCT_NOT_FOUND);
                        return Mono.error(new ProductNotFoundException(ProductConstants.MSG_PRODUCT_NOT_FOUND));
                    }
                    return Mono.empty();
                })
                .then()
                .onErrorResume(error ->{
                    if (error instanceof ProductNotFoundException) {
                        log.error(error.getMessage());
                        return Mono.error(error);
                    }
                    log.error(ProductConstants.LOG_ERROR_VALIDATING_PRODUCT_EXISTENCE, error);
                    return Mono.error(new ValidateExistenceFailed(ProductConstants.MSG_ERROR_VALIDATING_PRODUCT_EXISTENCE));
                });
    }

    @Override
    public Flux<Product> getProductsByIdList(Flux<String> idList) {
        return productRepositoryAdapter.getProductsByIdList(idList)
                .switchIfEmpty(Flux.error(new ProductNotFoundException(ProductConstants.MSG_PRODUCT_NOT_FOUND)))
                .onErrorResume(error -> {
                    if (error instanceof ProductNotFoundException) {
                        log.error(error.getMessage());
                        return Flux.error(error);
                    }
                    log.error(ProductConstants.LOG_ERROR_GET_PRODUCTS_BY_ID_LIST, error);
                    return Flux.error(new GetProductsByIdListFailed(ProductConstants.MSG_ERROR_GET_PRODUCTS_BY_ID_LIST));
                });
    }
}
