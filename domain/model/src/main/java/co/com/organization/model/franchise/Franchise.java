package co.com.organization.model.franchise;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder(toBuilder = true)
public class Franchise {
    private String id;
    private String name;
    List<String> branchIds;
}
