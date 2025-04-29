package get2gether.manualMapper;

import get2gether.dto.EventDto;
import get2gether.model.Event;
import org.springframework.stereotype.Service;

@Service
public class ManualEventMapper {

   public EventDto modelToDtoOnGet(Event event) {
        return EventDto.builder()
                .id(event.getId())
                .name(event.getName())
                .description(event.getDescription())
                .date(event.getDate())
                .build();
    }
}
