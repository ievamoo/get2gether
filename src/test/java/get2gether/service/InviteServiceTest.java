package get2gether.service;

import get2gether.dto.InviteDto;
import get2gether.enums.Type;
import get2gether.event.EventPublisher;
import get2gether.event.InviteStatusChangedEvent;
import get2gether.exception.ForbiddenActionException;
import get2gether.exception.ResourceNotFoundException;
import get2gether.mapper.InviteMapper;
import get2gether.model.*;
import get2gether.repository.InviteRepository;
import get2gether.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InviteServiceTest {

    @Mock
    private InviteRepository inviteRepository;

    @Mock
    private UserService userService;

    @Mock
    private InviteMapper inviteMapper;

    @Mock
    private GroupService groupService;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private InviteService inviteService;

    private User testUser;
    private User user1;
    private User user2;
    private Group testGroup;
    private Invite testInvite;

    @BeforeEach
    void setUp() {
        inviteService = new InviteService(inviteRepository, userService, inviteMapper,
                groupService, eventPublisher, userRepository, messagingTemplate);

        testUser = User.builder()
                .id(1L)
                .username("testuser@test.com")
                .firstName("Test")
                .lastName("User")
                .invitesReceived(new ArrayList<>())
                .groups(new HashSet<>())
                .build();

        user1 = User.builder()
                .id(2L)
                .username("user1@test.com")
                .firstName("User")
                .lastName("One")
                .invitesReceived(new ArrayList<>())
                .groups(new HashSet<>())
                .build();

        user2 = User.builder()
                .id(3L)
                .username("user2@test.com")
                .firstName("User")
                .lastName("Two")
                .invitesReceived(new ArrayList<>())
                .groups(new HashSet<>())
                .build();

        testGroup = Group.builder()
                .id(1L)
                .name("Test Group")
                .admin(testUser)
                .members(new HashSet<>(Set.of(testUser)))
                .build();

        testInvite = Invite.builder()
                .id(1L)
                .type(Type.GROUP)
                .typeId(testGroup.getId())
                .typeName(testGroup.getName())
                .senderUsername(testUser.getUsername())
                .receiver(user1)
                .build();
    }

    @Test
    void createNewInviteWhenGroupAlreadyExists_Success() {
        // Arrange
        InviteDto inviteDto = InviteDto.builder()
                .type(Type.GROUP)
                .typeId(testGroup.getId())
                .typeName(testGroup.getName())
                .receiverUsernames(Set.of(user1.getUsername(), user2.getUsername()))
                .build();

        when(groupService.getGroupByIdFromDb(testGroup.getId())).thenReturn(testGroup);
        when(userService.getUserFromDb(testUser.getUsername())).thenReturn(testUser);
        when(userRepository.findByUsername(user1.getUsername())).thenReturn(Optional.of(user1));
        when(userRepository.findByUsername(user2.getUsername())).thenReturn(Optional.of(user2));
        when(inviteMapper.dtoToModel(eq(testGroup.getId()), any(), eq(testUser.getUsername()), eq(testGroup.getName())))
                .thenReturn(testInvite);
        when(inviteRepository.save(any(Invite.class))).thenReturn(testInvite);
        when(inviteMapper.modelToDto(testInvite)).thenReturn(inviteDto);

        // Act
        String result = inviteService.createNewInviteWhenGroupAlreadyExists(inviteDto, testUser.getUsername());

        // Assert
        assertEquals("Invite(s) were sent successfully", result);
        verify(inviteRepository, times(2)).save(any(Invite.class));
        verify(messagingTemplate, times(2)).convertAndSendToUser(
                anyString(), eq("/queue/invites"), any(InviteDto.class));
    }

    @Test
    void createNewInviteWhenGroupAlreadyExists_NonGroupInvite() {
        // Arrange
        InviteDto inviteDto = InviteDto.builder()
                .type(Type.EVENT)
                .typeId(1L)
                .build();

        // Act & Assert
        assertThrows(ForbiddenActionException.class, () ->
                inviteService.createNewInviteWhenGroupAlreadyExists(inviteDto, testUser.getUsername()));
    }

    @Test
    void createNewInviteWhenGroupAlreadyExists_NonMemberSender() {
        // Arrange
        InviteDto inviteDto = InviteDto.builder()
                .type(Type.GROUP)
                .typeId(testGroup.getId())
                .build();

        User nonMember = User.builder()
                .username("nonmember@test.com")
                .build();

        when(groupService.getGroupByIdFromDb(testGroup.getId())).thenReturn(testGroup);
        when(userService.getUserFromDb("nonmember@test.com")).thenReturn(nonMember);

        // Act & Assert
        assertThrows(ForbiddenActionException.class, () ->
                inviteService.createNewInviteWhenGroupAlreadyExists(inviteDto, "nonmember@test.com"));
    }

    @Test
    void handleInviteResponse_Accept() {
        // Arrange
        user1.getInvitesReceived().add(testInvite);
        when(userService.getUserFromDb(user1.getUsername())).thenReturn(user1);

        // Act
        inviteService.handleInviteResponse(user1.getUsername(), testInvite.getId(), true);

        // Assert
        verify(inviteRepository).delete(testInvite);
        verify(eventPublisher).publishInviteStatusChangedEvent(any(InviteStatusChangedEvent.class));
        assertTrue(user1.getInvitesReceived().isEmpty());
    }

    @Test
    void handleInviteResponse_InviteNotFound() {
        // Arrange
        when(userService.getUserFromDb(user1.getUsername())).thenReturn(user1);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                inviteService.handleInviteResponse(user1.getUsername(), 999L, true));
    }

    @Test
    void createInvitesOnGroupCreation_Success() {
        // Arrange
        Set<String> invitedUsernames = Set.of(user1.getUsername(), user2.getUsername());
        when(userRepository.findByUsername(user1.getUsername())).thenReturn(Optional.of(user1));
        when(userRepository.findByUsername(user2.getUsername())).thenReturn(Optional.of(user2));
        when(inviteMapper.dtoToModel(eq(testGroup.getId()), any(), eq(testUser.getUsername()), eq(testGroup.getName())))
                .thenReturn(testInvite);
        when(inviteRepository.save(any(Invite.class))).thenReturn(testInvite);
        when(inviteMapper.modelToDto(testInvite)).thenReturn(InviteDto.builder().build());

        // Act
        List<InviteDto> result = inviteService.createInvitesOnGroupCreation(testGroup, invitedUsernames);

        // Assert
        assertEquals(2, result.size());
        verify(inviteRepository, times(2)).save(any(Invite.class));
    }

    @Test
    void getInvitesByTypeAndTypeId_Success() {
        // Arrange
        List<Invite> expectedInvites = List.of(testInvite);
        when(inviteRepository.findByTypeAndTypeId(Type.GROUP, testGroup.getId())).thenReturn(expectedInvites);

        // Act
        List<Invite> result = inviteService.getInvitesByTypeAndTypeId(Type.GROUP, testGroup.getId());

        // Assert
        assertEquals(expectedInvites, result);
        verify(inviteRepository).findByTypeAndTypeId(Type.GROUP, testGroup.getId());
    }

    @Test
    void findByReceiverAndTypeAndTypeId_Success() {
        // Arrange
        when(inviteRepository.findByReceiverAndTypeAndTypeId(user1, Type.GROUP, testGroup.getId()))
                .thenReturn(Optional.of(testInvite));

        // Act
        Optional<Invite> result = inviteService.findByReceiverAndTypeAndTypeId(user1, Type.GROUP, testGroup.getId());

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testInvite, result.get());
    }
}