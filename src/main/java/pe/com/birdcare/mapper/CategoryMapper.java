package pe.com.birdcare.mapper;

import org.mapstruct.Mapper;
import pe.com.birdcare.dto.CategoryResponseDTO;
import pe.com.birdcare.entity.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper{
    CategoryResponseDTO toResponse(Category category);
}