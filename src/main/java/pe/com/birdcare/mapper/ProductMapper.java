package pe.com.birdcare.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import pe.com.birdcare.dto.ProductRequestDTO;
import pe.com.birdcare.dto.ProductResponseDTO;
import pe.com.birdcare.entity.Product;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    //CREATE REQUEST->PRODUCT
    //We ignore category in the target because we have to do the transformation from id to Category manually
    @Mapping(target = "category", ignore = true)
    Product toEntity(ProductRequestDTO req);

    //UPDATE REQUEST->PRODUCT
    //void because we are just updating the existing object
    @Mapping(target = "category", ignore = true)
    void updateProduct(ProductRequestDTO req, @MappingTarget Product target);

    //JSON RESPONSE, JUST NECESSARY INFORMATION
    // PRODUCT->RESPONSE
    @Mapping(source = "category.name", target = "categoryName")
    ProductResponseDTO toResponse(Product target);

}
