package get2gether.manualMapper;

import get2gether.dto.EventDto;
import get2gether.exception.ResourceNotFoundException;
import get2gether.model.Event;
import get2gether.model.Group;
import get2gether.model.ResourceType;
import get2gether.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ManualEventMapper {

    private final UserRepository userRepository;

   public EventDto modelToDtoOnGet(Event event) {
        return EventDto.builder()
                .id(event.getId())
                .name(event.getName())
                .description(event.getDescription())
                .hostUsername(formatHost(event.getHostUsername()))
                .groupName(event.getGroup().getName())
                .date(event.getDate())
                .build();
    }

    public Event dtoToModel(EventDto dto) {
       return Event.builder()
               .name(dto.getName())
               .date(dto.getDate())
               .description(dto.getDescription())
               .build();
    }

    public void updateEvent(EventDto dto, Event event) {
        event.setDate(dto.getDate());
        event.setName(dto.getName());
        event.setDescription(dto.getDescription());
    }

    private String formatHost(String hostUsername) {
        var user = userRepository.findByUsername(hostUsername).orElseThrow(
                () -> new ResourceNotFoundException(ResourceType.USER, "username:" + hostUsername));
        return String.format("%s %s", user.getFirstName(), user.getLastName());
    }


}
