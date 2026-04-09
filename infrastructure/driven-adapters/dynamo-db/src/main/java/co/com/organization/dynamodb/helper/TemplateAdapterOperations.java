package co.com.organization.dynamodb.helper;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchGetItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.PagePublisher;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.ReadBatch;
import software.amazon.awssdk.enhanced.dynamodb.model.WriteBatch;

import java.lang.reflect.ParameterizedType;
import java.util.List;

public abstract class TemplateAdapterOperations< E> {
    private final Class<E> dataClass;
    private final DynamoDbAsyncTable<E> table;
    private final DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;

    @SuppressWarnings("unchecked")
    protected TemplateAdapterOperations(DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient,
                                        String tableName) {
        ParameterizedType genericSuperclass = (ParameterizedType) this.getClass().getGenericSuperclass();
        this.dataClass = (Class<E>) genericSuperclass.getActualTypeArguments()[2];
        this.dynamoDbEnhancedAsyncClient = dynamoDbEnhancedAsyncClient;
        table = dynamoDbEnhancedAsyncClient.table(tableName, TableSchema.fromBean(dataClass));
    }

    public Mono<Boolean> exists(Key key) {
        return Mono.fromFuture(table.getItem(key)).hasElement();
    }

    public Mono<E> getItem(Key key) {
        return Mono.fromFuture(table.getItem(key));
    }

    public Mono<E> deleteItem(Key key) {
        return Mono.fromFuture(table.deleteItem(key));
    }

    public Mono<E> save(E entity) {
        return Mono.fromFuture(table.putItem(entity)).thenReturn(entity);
    }

    public Mono<List<E>> query(QueryEnhancedRequest queryExpression) {
        PagePublisher<E> pagePublisher = table.query(queryExpression);
        return Mono.from(pagePublisher).map(page -> page.items().stream().toList());
    }

    public Flux<E> queryByPk(String pk) {
        QueryEnhancedRequest request = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(
                        Key.builder().partitionValue(pk).build()
                ))
                .build();
        return Flux.from(table.query(request).flatMapIterable(page -> page.items()));
    }

    public Mono<Void> batchDelete(List<Key> keys) {
        return Flux.fromIterable(keys)
                .buffer(25)
                .flatMap(batch -> {
                    WriteBatch writeBatch = batch.stream()
                            .reduce(
                                    WriteBatch.builder(dataClass).mappedTableResource(table),
                                    WriteBatch.Builder::addDeleteItem,
                                    (b1, b2) -> b1
                            ).build();
                    return Mono.fromFuture(dynamoDbEnhancedAsyncClient.batchWriteItem(
                            BatchWriteItemEnhancedRequest.builder()
                                    .writeBatches(writeBatch)
                                    .build()
                    ));
                })
                .then();
    }

    public Mono<List<E>> batchGetItem(List<Key> keys) {
        ReadBatch readBatch = keys.stream()
                .reduce(
                        ReadBatch.builder(dataClass).mappedTableResource(table),
                        ReadBatch.Builder::addGetItem,
                        (b1, b2) -> b1
                ).build();

        return Flux.from(dynamoDbEnhancedAsyncClient.batchGetItem(BatchGetItemEnhancedRequest.builder()
                                .readBatches(readBatch)
                                .build())
                        .resultsForTable(table))
                .collectList();
    }
}