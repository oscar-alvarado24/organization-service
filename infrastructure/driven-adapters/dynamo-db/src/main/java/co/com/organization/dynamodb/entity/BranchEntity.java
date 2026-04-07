package co.com.organization.dynamodb.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@DynamoDbBean
@AllArgsConstructor
@Builder(toBuilder = true)
public class BranchEntity {
    private String branchId;
    private String sk;
    @Getter
    private String name;
    @Getter
    private String address;
    @Getter
    private String phone;
    @Getter
    private String productId;
    @Getter
    private int stock;

    @DynamoDbPartitionKey
    public String getBranchId() {
        return branchId;
    }

    @DynamoDbSortKey
    public String getSk() {
        return sk;
    }
}
