package get2gether.manualMapper;

import get2gether.dto.EventDto;
import get2gether.model.Event;
import get2gether.model.Group;
import org.springframework.stereotype.Service;

@Service
public class ManualEventMapper {

   public EventDto modelToDtoOnGet(Event event) {
        return EventDto.builder()
                .id(event.getId())
                .name(event.getName())
                .description(event.getDescription())
                .hostUsername(event.getHostUsername())
                .groupId(event.getGroup().getId())
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
}
