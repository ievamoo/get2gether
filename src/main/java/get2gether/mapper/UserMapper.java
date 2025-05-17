package get2gether.mapper;

import get2gether.dto.GroupDto;
import get2gether.dto.UserDto;
import get2gether.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class UserMapper {

    private final InviteMapper inviteMapper;
    private final EventMapper eventMapper;

    public UserDto modelToDtoOnGroupCreate(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }

    public UserDto modelToDtoOnGetUser(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .availableDays(user.getAvailableDays())
                .groups(user.getGroups().stream()
                        .map(group -> GroupDto.builder()
                                .id(group.getId())
                                .name(group.getName())
                                .groupColor(group.getGroupColor())
                                .build())
                        .collect(Collectors.toSet()))
                .invitesReceived(user.getInvitesReceived().stream()
                        .map(inviteMapper::modelToDto)
                        .toList())
                .goingEvents(user.getGoingEvents().stream().map(
                        eventMapper::modelToDtoOnGet
                ).collect(Collectors.toList()))
                .build();
    }

    public void updateCurrentUser(UserDto dto, User user) {
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
    }

}
