package get2gether.manualMapper;

import get2gether.dto.UserDto;
import get2gether.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ManualUserMapper {

    public UserDto modelToDtoOnGroupCreate(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }

}
