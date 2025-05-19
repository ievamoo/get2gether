package get2gether;

import get2gether.dto.MessageDto;
import get2gether.dto.RegisterRequestDto;
import get2gether.dto.UserDto;
import get2gether.model.Group;
import get2gether.model.Message;
import get2gether.model.Role;
import get2gether.model.User;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class TestData {


    public static User getTestUser() {
        return User.builder()
                .id(1L)
                .username("test@gmail.com")
                .firstName("TestName")
                .lastName("TestLastName")
                .password("encoded_password")
                .roles(List.of(Role.USER))
                .availableDays(new HashSet<>())
                .groups(new HashSet<>())
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
                .availableDays(new HashSet<>())
                .groups(new HashSet<>())
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

    public static MessageDto getMessageDto() {
        return MessageDto.builder()
                .message("Hello Group!")
                .build();
    }

    public static Message getMessage() {
        return Message.builder()
                .id(1L)
                .message("Hello Group!")
                .senderUsername("test@gmail.com")
                .build();
    }


    public static Group getGroup() {
        return Group.builder()
                .id(1L)
                .name("Test Group")
                .members(Set.of(getTestUser()))
                .build();
    }

    public static Group getGroupWithoutMembers() {
        return Group.builder()
                .id(2L)
                .name("New Group")
                .members(Set.of())
                .build();
    }
}
