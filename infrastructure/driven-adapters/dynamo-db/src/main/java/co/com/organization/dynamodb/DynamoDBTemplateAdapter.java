package co.com.organization.dynamodb;

import co.com.organization.dynamodb.entity.BranchEntity;
import co.com.organization.dynamodb.helper.BranchConstants;
import co.com.organization.dynamodb.helper.TemplateAdapterOperations;
import co.com.organization.dynamodb.mapper.BranchEntityMapper;
import co.com.organization.exception.SaveBranchFailed;
import co.com.organization.model.branch.Branch;
import co.com.organization.model.branch.ProductBranch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;


@Repository
@Slf4j
public class DynamoDBTemplateAdapter extends TemplateAdapterOperations<BranchEntity>  {
    private final BranchEntityMapper mapper;
    public DynamoDBTemplateAdapter(DynamoDbEnhancedAsyncClient connectionFactory, BranchEntityMapper mapper) {
        super(connectionFactory, BranchConstants.TABLE_NAME);
        this.mapper = mapper;
    }

    public Mono<List<Branch>> getTopProductList(List<String> branchIds) {
        List<Key> keys = branchIds.stream()
                .flatMap(branchId -> Stream.of(
                        Key.builder()
                                .partitionValue(branchId)
                                .sortValue(BranchConstants.BRANCH_METADATA_LABEL)
                                .build(),
                        Key.builder()
                                .partitionValue(branchId)
                                .sortValue(BranchConstants.TOP_PRODUCT_LABEL)
                                .build()
                ))
                .toList();

        return batchGetItem(keys)
                .map(mapper::toBranchList);
    }

    @SuppressWarnings("PointlessBooleanExpression")
    public Mono<Boolean> saveBranch(Mono<Branch> branch) {
        return branch
                .map(mapper::toBranchEntity)
                .flatMap(this::save)
                .hasElement()
                .flatMap(bool->{
                    if (Boolean.FALSE.equals(bool)){
                        return Mono.error(new SaveBranchFailed(BranchConstants.MSG_ERROR_SAVING_BRANCH));
                    }
                    return Mono.just(Boolean.TRUE);
                });

    }

    public Mono<String> addProductToBranch(String branchId, ProductBranch product) {
        Key topProductKey = Key.builder()
                .partitionValue(branchId)
                .sortValue(BranchConstants.TOP_PRODUCT_LABEL)
                .build();

        return getItem(topProductKey)
                .flatMap(topProductSaved -> {
                    Mono<String> saveProduct = save(mapper.toProductEntity(branchId, product))
                            .map(BranchEntity::getProductId);
                    if (topProductSaved.getStock() >= product.getStock()) return saveProduct;
                    return deleteItem(topProductKey)
                            .then(save(mapper.toTopProductEntity(branchId, product)))
                            .then(saveProduct);
                })
                .switchIfEmpty(
                    save(mapper.toTopProductEntity(branchId, product))
                            .then(save(mapper.toProductEntity(branchId, product)))
                            .map(BranchEntity::getProductId)
                );
    }

    public Mono<String> deleteProductOfBranch(String branchId, String productId) {
        Key topProductKey = Key.builder()
                .partitionValue(branchId)
                .sortValue(BranchConstants.TOP_PRODUCT_LABEL)
                .build();

        return getItem(topProductKey)
                .flatMap(topProductSaved -> {
                    if (!topProductSaved.getProductId().equals(productId)) {
                        return deleteItem(Key.builder()
                                .partitionValue(branchId)
                                .sortValue(BranchConstants.BRANCH_PRODUCT_LABEL + productId)
                                .build())
                                .thenReturn(productId);
                    }
                    return deleteItem(topProductKey)
                            .flatMap(p->{
                                QueryEnhancedRequest request = QueryEnhancedRequest.builder()
                                        .queryConditional(QueryConditional.sortBeginsWith(
                                                Key.builder().partitionValue(branchId).sortValue(BranchConstants.BRANCH_PRODUCT_LABEL).build()
                                        ))
                                        .build();
                                return query(request).flatMap(products ->{
                                    Optional<BranchEntity> maxStock = products.stream()
                                            .max(Comparator.comparingInt(BranchEntity::getStock));
                                    if (maxStock.isPresent()){
                                        BranchEntity newTopProduct = maxStock.get().toBuilder().sk(BranchConstants.TOP_PRODUCT_LABEL).build();
                                        return save(newTopProduct);
                                    }
                                    return Mono.<BranchEntity>empty().switchIfEmpty(Mono.fromRunnable(() ->
                                            log.info("No hay productos para branchId: {}", branchId)
                                    ).then(Mono.empty()));
                                });
                            })
                            .thenReturn(BranchConstants.MSG_PRODUCT_DELETED_SUCCESSFULLY);
                })
                .switchIfEmpty(Mono.just(BranchConstants.MSG_BRANCH_WITH_OUT_PRODUCTS));
    }

    public Mono<Void> deleteBranch(String branchId) {
        return queryByPk(branchId)
                .map(entity -> Key.builder()
                        .partitionValue(entity.getBranchId())
                        .sortValue(entity.getSk())
                        .build())
                .collectList()
                .flatMap(this::batchDelete);
    }

    public Mono<String> changeProductStock(String branchId, String productId, int stock) {
        Key productKey = Key.builder()
                .partitionValue(branchId)
                .sortValue(BranchConstants.BRANCH_PRODUCT_LABEL + productId)
                .build();
        return getItem(productKey)
                .flatMap(product -> {
                    BranchEntity updatedProduct = product.toBuilder().stock(stock).build();
                    return save(updatedProduct).thenReturn(BranchConstants.MSG_CHANGE_PRODUCT_STOCK_SUCCESSFULLY);
                });
    }

    public Mono<String> changeBranchName(String branchId, String newName) {
         Key branchKey = Key.builder()
                .partitionValue(branchId)
                .sortValue(BranchConstants.BRANCH_METADATA_LABEL)
                .build();
        return getItem(branchKey)
                .flatMap(branch -> {
                    BranchEntity updatedBranch = branch.toBuilder().name(newName).build();
                    return save(updatedBranch).thenReturn(BranchConstants.MSG_CHANGE_BRANCH_NAME_SUCCESSFULLY);
                });
    }
}
