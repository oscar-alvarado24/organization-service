package co.com.organization.dynamodb.mapper;

import co.com.organization.dynamodb.entity.BranchEntity;
import co.com.organization.dynamodb.helper.BranchConstants;
import co.com.organization.model.branch.Branch;
import co.com.organization.model.branch.ProductBranch;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface BranchEntityMapper {

    default List<Branch> toBranchList(List<BranchEntity> branchList) {
        if (branchList == null || branchList.isEmpty()) {
            return new ArrayList<>();
        }
        List<Branch> branches = new ArrayList<>();
        Map<String, List<BranchEntity>> groupedByBranchId = branchList.stream()
                .collect(Collectors.groupingBy(BranchEntity::getBranchId));

        groupedByBranchId.forEach((branchId, entities) -> branches.add(generateBranch(entities)));
        return branches;
    }

    private Branch generateBranch(List<BranchEntity> entities) {
        Branch branch = new Branch();
        List<ProductBranch> products = new ArrayList<>();

        entities.forEach(entity -> {

            if (entity.getSk().equals(BranchConstants.BRANCH_METADATA_LABEL)) {
                branch.setBranchId(entity.getBranchId());
                branch.setName(entity.getName());
                branch.setAddress(entity.getAddress());
                branch.setPhone(entity.getPhone());
            } else {
                products.add(new ProductBranch(entity.getProductId(), entity.getStock()));
            }
        });
        branch.setProducts(products);
        return branch;
    }

    @Mapping(target = "sk", expression = "java(co.com.organization.dynamodb.helper.BranchConstants.BRANCH_METADATA_LABEL)")
    @Mapping(target = "productId", ignore = true)
    @Mapping(target = "stock", ignore = true)
    BranchEntity toBranchEntity(Branch branch);

    default BranchEntity toProductEntity(String branchId, ProductBranch product) {
        return BranchEntity.builder()
                .branchId(branchId)
                .sk(BranchConstants.BRANCH_PRODUCT_LABEL + product.getId())
                .productId(product.getId())
                .stock(product.getStock())
                .build();
    }

    default BranchEntity toTopProductEntity(String branchId, ProductBranch product) {
        return BranchEntity.builder()
                .branchId(branchId)
                .sk(BranchConstants.TOP_PRODUCT_LABEL)
                .productId(product.getId())
                .stock(product.getStock())
                .build();
    }
}
