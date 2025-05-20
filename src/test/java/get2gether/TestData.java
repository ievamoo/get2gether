package get2gether;

import get2gether.dto.EventDto;
import get2gether.dto.MessageDto;
import get2gether.dto.RegisterRequestDto;
import get2gether.dto.UserDto;
import get2gether.model.*;

import java.time.LocalDate;
import java.util.ArrayList;
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

    public static User getNotHostUser(){
        return User.builder()
                .username("other@test.com")
                .firstName("Other")
                .lastName("User")
                .password("encoded_password")
                .roles(List.of(Role.USER))
                .goingEvents(new ArrayList<>())
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

    public static Group getGroupWithoutId(){
        return Group.builder()
                .name("Integration test group")
                .members(Set.of(getTestUser()))
                .admin(getTestUser())
                .build();
    }

    public static EventDto getEventDto(){
        return EventDto.builder()
                .name("Liverpool's parade")
                .date(LocalDate.now().plusDays(1))
                .description("Premier League victory parade")
                .groupName("Integration test group")
                .hostUsername("test@gmail.com")
                .build();
    }

    public static Event getEvent(){
        return Event.builder()
                .name("Liverpool's parade")
                .date(LocalDate.now().plusDays(1))
                .description("Premier League victory parade")
                .hostUsername("test@gmail.com")
                .build();
    }

    public static Event getSavedEvent(){
        return Event.builder()
                .id(1L)
                .name("Liverpool's parade")
                .date(LocalDate.now().plusDays(1))
                .description("Premier League victory parade")
                .hostUsername("test@gmail.com")
                .goingMembers(new HashSet<>())
                .build();
    }

    public static EventDto getPastEventDto(){
       return EventDto.builder()
                .name("Old Event")
                .date(LocalDate.now().minusDays(1))  // Date in the past
                .description("This event should not be allowed")
                .groupName("Integration test group")
                .build();
    }

    public static Invite getInvite() {
        return Invite.builder()
                .id(1L)
                .receiver(getNotHostUser())
                .senderUsername(getTestUser().getUsername())
                .type(Type.EVENT)
                .typeId(1L)
                .build();
    }

}
