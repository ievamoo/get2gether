package get2gether.util;

import get2gether.dto.UserDto;
import get2gether.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto modelToDto(User user);

    default void updateUserProfile(UserDto dto, @MappingTarget User entity) {
        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
    }


}
