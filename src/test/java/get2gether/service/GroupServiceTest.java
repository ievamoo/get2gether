package get2gether.service;

import get2gether.dto.GroupDto;
import get2gether.dto.UserDto;
import get2gether.event.EventPublisher;
import get2gether.exception.ForbiddenActionException;
import get2gether.exception.ResourceAlreadyExistsException;
import get2gether.mapper.EventMapper;
import get2gether.mapper.GroupMapper;
import get2gether.mapper.UserMapper;
import get2gether.model.*;
import get2gether.repository.EventRepository;
import get2gether.repository.GroupRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private UserService userService;

    @Mock
    private GroupMapper groupMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private EventMapper eventMapper;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private GroupService groupService;

    private User adminUser;
    private User memberUser;
    private Group testGroup;
    private GroupDto testGroupDto;
    private GroupDto testGroupDtoResult;

    @BeforeEach
    void setUp() {
        // Create admin user
        adminUser = User.builder()
                .id(1L)
                .username("admin@example.com")
                .firstName("Admin")
                .lastName("User")
                .password("encoded_password")
                .roles(new ArrayList<>(List.of(Role.USER)))
                .availableDays(new HashSet<>())
                .groups(new HashSet<>())
                .goingEvents(new ArrayList<>())
                .build();

        // Create member user
        memberUser = User.builder()
                .id(2L)
                .username("member@example.com")
                .firstName("Member")
                .lastName("User")
                .password("encoded_password")
                .roles(new ArrayList<>(List.of(Role.USER)))
                .availableDays(new HashSet<>())
                .groups(new HashSet<>())
                .goingEvents(new ArrayList<>())
                .build();

        // Create test group
        testGroup = Group.builder()
                .id(1L)
                .name("Test Group")
                .admin(adminUser)
                .members(new HashSet<>(Set.of(adminUser)))
                .events(new ArrayList<Event>())
                .messages(new ArrayList<Message>())
                .groupColor("#FF0000")
                .build();
        
        // Add group to admin's groups
        adminUser.getGroups().add(testGroup);

        testGroupDto = GroupDto.builder()
                .name("New Test Group")
                .groupColor("#00FF00")
                .build();
        
        testGroupDtoResult = GroupDto.builder()
                .id(1L)
                .name("Test Group")
                .groupColor("#FF0000")
                .build();
    }

    @Test
    void getGroupById_ShouldReturnGroup_WhenGroupExists() {
        // Arrange
        Map<LocalDate, Set<UserDto>> availableDays = new HashMap<>();
        
        // Create a mock GroupDto instead of a real one
        GroupDto mockGroupDto = mock(GroupDto.class);
        
        when(groupRepository.findById(1L)).thenReturn(Optional.of(testGroup));
        when(groupMapper.modelToDtoOnGet(testGroup)).thenReturn(mockGroupDto);
        
        // Now we can mock the setGroupAvailability method since mockGroupDto is a mock
        when(mockGroupDto.setGroupAvailability(any())).thenReturn(mockGroupDto);
        
        // For assertions, we need to define what the mock returns for getName and getGroupColor
        when(mockGroupDto.getName()).thenReturn(testGroup.getName());
        when(mockGroupDto.getGroupColor()).thenReturn(testGroup.getGroupColor());

        // Act
        GroupDto result = groupService.getGroupById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(testGroup.getName());
        assertThat(result.getGroupColor()).isEqualTo(testGroup.getGroupColor());
        verify(groupRepository).findById(1L);
    }

    @Test
    void getGroupById_ShouldThrowException_WhenGroupNotFound() {
        // Arrange
        when(groupRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> groupService.getGroupById(999L));
        verify(groupRepository).findById(999L);
    }

    @Test
    void createGroup_ShouldCreateNewGroup_WhenNameNotTaken() {
        // Arrange
        Set<String> invitedUsernames = new HashSet<>();
        testGroupDto.setInvitedUsernames(invitedUsernames);
        
        when(groupRepository.existsByName(testGroupDto.getName())).thenReturn(false);
        when(userService.getUserFromDb(adminUser.getUsername())).thenReturn(adminUser);
        when(groupMapper.dtoToModelOnGroupCreate(testGroupDto, adminUser)).thenReturn(testGroup);
        when(groupRepository.save(testGroup)).thenReturn(testGroup);
        when(groupMapper.modelToDtoOnGroupCreate(testGroup)).thenReturn(testGroupDtoResult);
        doNothing().when(eventPublisher).publishGroupCreatedEvent(any());

        // Act
        GroupDto result = groupService.createGroup(adminUser.getUsername(), testGroupDto);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(testGroupDtoResult.getName());
        assertThat(result.getGroupColor()).isEqualTo(testGroupDtoResult.getGroupColor());
        
        verify(groupRepository).existsByName(testGroupDto.getName());
        verify(userService).getUserFromDb(adminUser.getUsername());
        verify(groupRepository).save(any(Group.class));
        verify(eventPublisher).publishGroupCreatedEvent(any());
    }

    @Test
    void createGroup_ShouldThrowException_WhenNameAlreadyExists() {
        // Arrange
        GroupDto duplicateGroupDto = GroupDto.builder()
                .name(testGroup.getName()) // Using existing group name
                .groupColor("#00FF00")
                .build();
        
        when(groupRepository.existsByName(testGroup.getName())).thenReturn(true);

        // Act & Assert
        assertThrows(ResourceAlreadyExistsException.class, 
            () -> groupService.createGroup(adminUser.getUsername(), duplicateGroupDto));
        
        verify(groupRepository).existsByName(testGroup.getName());
        verify(groupRepository, never()).save(any(Group.class));
    }

    @Test
    void updateGroup_ShouldUpdateGroupDetails() {
        // Arrange
        GroupDto updateDto = GroupDto.builder()
                .name("Updated Group Name")
                .groupColor("#0000FF")
                .build();
        
        GroupDto updatedGroupResult = GroupDto.builder()
                .id(1L)
                .name("Updated Group Name")
                .groupColor("#0000FF")
                .build();
        
        when(groupRepository.findById(1L)).thenReturn(Optional.of(testGroup));
        when(groupRepository.save(any(Group.class))).thenReturn(testGroup);
        when(groupMapper.modelToDtoOnUpdate(testGroup)).thenReturn(updatedGroupResult);

        // Act
        GroupDto result = groupService.updateGroup(updateDto, 1L);

        // Assert
        assertThat(result.getName()).isEqualTo("Updated Group Name");
        assertThat(result.getGroupColor()).isEqualTo("#0000FF");
        
        verify(groupRepository).findById(1L);
        verify(groupRepository).save(testGroup);
    }

    @Test
    void leaveGroup_ShouldSucceed_WhenUserIsMember() {
        // Arrange
        testGroup.getMembers().add(memberUser);
        memberUser.getGroups().add(testGroup);
        
        List<Event> eventsToRemove = new ArrayList<>();
        
        when(groupRepository.findById(1L)).thenReturn(Optional.of(testGroup));
        when(userService.getUserFromDb(memberUser.getUsername())).thenReturn(memberUser);
        when(groupRepository.save(any(Group.class))).thenReturn(testGroup);
        doNothing().when(eventPublisher).publishGroupLeaveEvent(any());
        when(eventRepository.saveAll(any())).thenReturn(eventsToRemove);

        // Act
        groupService.leaveGroup(1L, memberUser.getUsername());

        // Assert
        verify(groupRepository).findById(1L);
        verify(userService).getUserFromDb(memberUser.getUsername());
        verify(groupRepository).save(any(Group.class));
        verify(eventPublisher).publishGroupLeaveEvent(any());
    }

    @Test
    void leaveGroup_ShouldThrowException_WhenUserIsAdmin() {
        // Arrange
        when(groupRepository.findById(1L)).thenReturn(Optional.of(testGroup));
        when(userService.getUserFromDb(adminUser.getUsername())).thenReturn(adminUser);

        // Act & Assert
        assertThrows(ForbiddenActionException.class, 
            () -> groupService.leaveGroup(1L, adminUser.getUsername()));
        
        verify(groupRepository).findById(1L);
        verify(userService).getUserFromDb(adminUser.getUsername());
        verify(groupRepository, never()).save(any(Group.class));
    }

    @Test
    void deleteGroup_ShouldSucceed_WhenUserIsAdmin() {
        // Arrange
        when(groupRepository.findById(1L)).thenReturn(Optional.of(testGroup));
        // Remove this stubbing since it's not being used
        // when(userService.getUserFromDb(adminUser.getUsername())).thenReturn(adminUser);
        doNothing().when(groupRepository).deleteById(1L);
        doNothing().when(eventPublisher).publishGroupDeletedEvent(any());

        // Act
        groupService.deleteGroup(1L, adminUser.getUsername());

        // Assert
        verify(groupRepository).findById(1L);
        // Remove this verification since the method doesn't call userService.getUserFromDb
        // verify(userService).getUserFromDb(adminUser.getUsername());
        verify(groupRepository).deleteById(1L);
        verify(eventPublisher).publishGroupDeletedEvent(any());
    }

    @Test
    void deleteGroup_ShouldThrowException_WhenUserIsNotAdmin() {
        // Arrange
        when(groupRepository.findById(1L)).thenReturn(Optional.of(testGroup));
        
        // Remove this stubbing as it's not needed
        // when(userService.getUserFromDb(memberUser.getUsername())).thenReturn(memberUser);

        // Act & Assert
        assertThrows(ForbiddenActionException.class, 
            () -> groupService.deleteGroup(1L, memberUser.getUsername()));
        
        verify(groupRepository).findById(1L);
        
        // Remove this verification as the method doesn't call userService.getUserFromDb
        // verify(userService).getUserFromDb(memberUser.getUsername());
        
        verify(groupRepository, never()).deleteById(anyLong());
    }

    @Test
    void addMember_ShouldSucceed_WhenUserNotInGroup() {
        // Arrange
        when(groupRepository.findById(1L)).thenReturn(Optional.of(testGroup));
        when(groupRepository.save(any(Group.class))).thenReturn(testGroup);

        // Act
        groupService.addMember(1L, memberUser);

        // Assert
        verify(groupRepository).findById(1L);
        verify(groupRepository).save(testGroup);
    }

    @Test
    void addMember_ShouldThrowException_WhenUserAlreadyInGroup() {
        // Arrange
        testGroup.getMembers().add(memberUser);
        memberUser.getGroups().add(testGroup);
        
        when(groupRepository.findById(1L)).thenReturn(Optional.of(testGroup));

        // Act & Assert
        assertThrows(ResourceAlreadyExistsException.class, 
            () -> groupService.addMember(1L, memberUser));
        
        verify(groupRepository).findById(1L);
        verify(groupRepository, never()).save(any(Group.class));
    }

    @Test
    void removeUserFromGroup_ShouldSucceed_WhenAdminRemovesMember() {
        // Arrange
        testGroup.getMembers().add(memberUser);
        memberUser.getGroups().add(testGroup);
        
        Set<UserDto> updatedMembers = new HashSet<>();
        UserDto adminUserDto = UserDto.builder().id(1L).username(adminUser.getUsername()).build();
        updatedMembers.add(adminUserDto);
        
        when(groupRepository.findById(1L)).thenReturn(Optional.of(testGroup));
        when(userService.getUserFromDb(memberUser.getUsername())).thenReturn(memberUser);
        // Removed the unnecessary stubbing
        when(groupRepository.save(any(Group.class))).thenReturn(testGroup);
        when(userMapper.modelToDtoOnGroupCreate(adminUser)).thenReturn(adminUserDto);

        // Act
        Set<UserDto> result = groupService.removeUserFromGroup(1L, memberUser.getUsername(), adminUser.getUsername());

        // Assert
        verify(groupRepository).findById(1L);
        verify(userService).getUserFromDb(memberUser.getUsername());
        verify(groupRepository).save(any(Group.class));
    }

    @Test
    void findByName_ShouldReturnGroup_WhenExists() {
        // Arrange
        when(groupRepository.findByName("Test Group")).thenReturn(Optional.of(testGroup));

        // Act
        Group result = groupService.findByName("Test Group");

        // Assert
        assertThat(result).isEqualTo(testGroup);
        verify(groupRepository).findByName("Test Group");
    }
}