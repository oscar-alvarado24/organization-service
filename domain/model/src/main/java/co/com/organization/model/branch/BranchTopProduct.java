package co.com.organization.model.branch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder(toBuilder = true)
public class BranchTopProduct {
    private String branchName;
    private String productId;
    private String productName;
}
