package get2gether.service;

import get2gether.dto.MessageDto;
import get2gether.exception.ForbiddenActionException;
import get2gether.mapper.MessageMapper;
import get2gether.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

    private final MessageRepository messageRepository;
    private final MessageMapper messageMapper;
    private final GroupService groupService;
    private final SimpMessagingTemplate messagingTemplate;

    public void save(Long groupId, MessageDto messageDto, String username) {
        log.info("sender is: {}", username);
        var group = groupService.getGroupByIdWithMembers(groupId);

        boolean isMember = group.getMembers().stream()
                        .anyMatch(user -> user.getUsername().equalsIgnoreCase(username));
        if (!isMember) {
            throw new ForbiddenActionException("Only group members are allowed to send messages.");
        }
        var savedMessage = messageRepository.save(messageMapper.dtoToModel(messageDto, username, group));
        var destination = "/topic/group/" + group.getId() + "/chat";
        messagingTemplate.convertAndSend(destination, messageMapper.modelToDto(savedMessage));
    }

}
