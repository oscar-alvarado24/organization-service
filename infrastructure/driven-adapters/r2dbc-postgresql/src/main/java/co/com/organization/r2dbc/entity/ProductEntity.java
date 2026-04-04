package co.com.organization.r2dbc.entity;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;
@Table("productos")
public record ProductEntity(
        @Id
        UUID id,
        String name,
        String description,
        Double price
) {}
