package get2gether.mapper;

import get2gether.dto.EventDto;
import get2gether.model.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(target = "goingMembers", ignore = true)
    EventDto modelToDtoOnGetUser(Event event);

    List<EventDto> toDtoList(List<Event> events);

}
