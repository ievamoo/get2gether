package get2gether.mapper;

import get2gether.dto.MessageDto;
import get2gether.exception.ResourceNotFoundException;
import get2gether.model.Group;
import get2gether.model.Message;
import get2gether.model.ResourceType;
import get2gether.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MessageMapper {

    private final UserRepository userRepository;

    public Message dtoToModel(MessageDto dto, String username, Group group) {
        return Message.builder()
                .group(group)
                .senderUsername(username)
                .message(dto.getMessage())
                .createdAt(LocalDateTime.now())
                .build();
    }

    public MessageDto modelToDto(Message savedMessage) {
        return MessageDto.builder()
                .id(savedMessage.getId())
                .groupId(savedMessage.getGroup().getId())
                .senderUsername(formatSender(savedMessage.getSenderUsername()))
                .message(savedMessage.getMessage())
                .createdAt(savedMessage.getCreatedAt())
                .build();
    }

    private String formatSender(String senderName) {
        var user = userRepository.findByUsername(senderName).orElseThrow(
                () -> new ResourceNotFoundException(ResourceType.USER, "username:" + senderName));
        return String.format("%s %s", user.getFirstName(), user.getLastName());
    }
}
