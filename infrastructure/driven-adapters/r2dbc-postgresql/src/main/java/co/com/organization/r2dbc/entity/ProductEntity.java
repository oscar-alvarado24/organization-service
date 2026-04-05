package co.com.organization.r2dbc.entity;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;
@Table("productos")
@Builder(toBuilder = true)
public record ProductEntity(
        @Id
        UUID id,
        String name,
        String description,
        Double price
) {}
