package co.com.organization.model.branch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder(toBuilder = true)
public class ProductBranch {
    private String id;
    private int stock;
}
