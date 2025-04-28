package get2gether;

import get2gether.dto.RegisterRequestDto;
import get2gether.dto.UserDto;
import get2gether.model.Role;
import get2gether.model.User;

import java.util.List;

public final class TestData {


    public static User getTestUser() {
        return User.builder()
                .id(1L)
                .username("test@gmail.com")
                .firstName("TestName")
                .lastName("TestLastName")
                .password("encoded_password")
                .roles(List.of(Role.USER))
                .build();
    }

    public static UserDto getTestUserDto() {
        return UserDto.builder()
                .id(1L)
                .username("test@gmail.com")
                .firstName("TestName")
                .lastName("TestLastName")
                .build();
    }

    public static User getSavedUser() {
        return User.builder()
                .id(1L)
                .username("test@gmail.com")
                .firstName("UpdatedTestName")
                .lastName("UpdatedTestLastName")
                .roles(List.of(Role.USER))
                .build();
    }

    public static UserDto getSavedUserDto() {
        return UserDto.builder()
                .id(1L)
                .username("test@gmail.com")
                .firstName("UpdatedTestName")
                .lastName("UpdatedTestLastName")
                .build();
    }

    public static RegisterRequestDto getRegisterRequestDto() {
        return RegisterRequestDto.builder()
                .username("newUser@mail.com")
                .firstName("NewUserName")
                .lastName("NewUserLastName")
                .password("newPassword")
                .build();
    }

}
