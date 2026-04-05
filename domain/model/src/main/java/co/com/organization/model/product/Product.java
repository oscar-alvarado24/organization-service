package co.com.organization.model.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode
public class Product {
    private String id;
    private String name;
    private String description;
    private Double price;
}
