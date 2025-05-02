package get2gether.mapper;

import get2gether.dto.AvailabilityDto;
import get2gether.model.Availability;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface AvailabilityMapper {

    @Named("modelToDto")
    AvailabilityDto modelToDto(Availability availability);

    @Named("dtoToModel")
    @Mapping(target = "id", ignore = true)
    Availability dtoToModel(AvailabilityDto availability);

}
