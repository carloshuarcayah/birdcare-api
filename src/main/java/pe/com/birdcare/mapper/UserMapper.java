package pe.com.birdcare.mapper;
import org.mapstruct.Mapper;
import pe.com.birdcare.dto.UserResponseDTO;
import pe.com.birdcare.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponseDTO response(User user);
}
