package get2gether.service;

import get2gether.TestData;
import get2gether.dto.MessageDto;
import get2gether.exception.ForbiddenActionException;
import get2gether.exception.ResourceNotFoundException;
import get2gether.mapper.MessageMapper;
import get2gether.model.Group;
import get2gether.model.Message;
import get2gether.enums.ResourceType;
import get2gether.model.User;
import get2gether.repository.MessageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;
    @Mock
    private MessageMapper messageMapper;
    @Mock
    private GroupService groupService;
    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private MessageService testMessageService;

    private final User user = TestData.getTestUser();
    private final Group group = TestData.getGroup();
    private final Group groupWithoutMembers = TestData.getGroupWithoutMembers();
    private final Message message = TestData.getMessage();
    private final MessageDto messageDto = TestData.getMessageDto();

    @Test
    void save_WhenUserIsGroupMember() {
        when(groupService.getGroupByIdWithMembers(1L)).thenReturn(group);
        when(messageMapper.dtoToModel(messageDto, user.getUsername(), group)).thenReturn(message);
        when(messageRepository.save(message)).thenReturn(message);
        when(messageMapper.modelToDto(message)).thenReturn(messageDto);

        testMessageService.save(group.getId(), messageDto, user.getUsername());

        String expectedDestination = "/topic/group/" + group.getId() + "/chat";
        verify(messagingTemplate).convertAndSend(expectedDestination, messageDto);
        verify(messageRepository).save(message);
    }

    @Test
    void save_WhenUserIsNotGroupMember_ShouldThrowForbiddenActionException() {
        when(groupService.getGroupByIdWithMembers(2L)).thenReturn(groupWithoutMembers);

        assertThrows(ForbiddenActionException.class, () ->
                testMessageService.save(groupWithoutMembers.getId(), messageDto, user.getUsername()));
    }

    @Test
    void save_WhenGroupDoesNotExist_ShouldThrowResourceNotFoundException() {
        Long invalidGroupId = 999L;

        when(groupService.getGroupByIdWithMembers(invalidGroupId))
                .thenThrow(new ResourceNotFoundException(ResourceType.GROUP, "id: " + invalidGroupId));

        assertThrows(ResourceNotFoundException.class, () ->
                testMessageService.save(invalidGroupId, messageDto, user.getUsername()));
    }

}