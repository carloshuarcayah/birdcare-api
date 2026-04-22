package pe.com.birdcare.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pe.com.birdcare.dto.ProductResponseDTO;
import pe.com.birdcare.entity.Product;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @Mapping(source = "category.name", target = "categoryName")
    ProductResponseDTO toResponse(Product target);
}
