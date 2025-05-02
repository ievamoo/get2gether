package get2gether.manualMapper;

import get2gether.dto.AvailabilityDto;
import get2gether.model.Availability;
import get2gether.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ManualAvailabilityMapper {

    public Availability dtoToModel(AvailabilityDto dto, User user) {
        return Availability.builder()
                .user(user)
                .availableDays(dto.getAvailableDays())
                .build();
    }

    public AvailabilityDto modelToDto(Availability availability) {
        return AvailabilityDto.builder()
                .availableDays(availability.getAvailableDays())
                .build();
    }
}
