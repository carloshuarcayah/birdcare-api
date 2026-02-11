package pe.com.birdcare.mapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import pe.com.birdcare.dto.UserCreateDTO;
import pe.com.birdcare.dto.UserResponseDTO;
import pe.com.birdcare.dto.UserUpdateDTO;
import pe.com.birdcare.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "password", ignore = true)
    void updateUser(UserUpdateDTO dto, @MappingTarget User user);

    @Mapping(target = "password", ignore = true)
    User createUser(UserCreateDTO req);

    UserResponseDTO response(User user);


}
