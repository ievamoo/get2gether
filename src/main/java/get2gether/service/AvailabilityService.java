package get2gether.service;

import get2gether.dto.AvailabilityDto;
import get2gether.manualMapper.ManualAvailabilityMapper;
import get2gether.model.Availability;
import get2gether.model.User;
import get2gether.repository.AvailabilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private final AvailabilityRepository availabilityRepository;
    private final UserService userService;
    private final ManualAvailabilityMapper manualAvailabilityMapper;

    @Transactional
    public AvailabilityDto addAvailableDay(String userName, AvailabilityDto availabilityDto) {
        var currentUser = userService.getUserFromDb(userName);
        var availableDays = availabilityRepository.findByUser(currentUser);
        var availability = availableDays.map(
                existingAvailability -> updateAvailability(availabilityDto, existingAvailability))
                .orElseGet(() -> createAvailability(availabilityDto, currentUser));
        return manualAvailabilityMapper.modelToDto(availability);
    }

    private Availability updateAvailability(AvailabilityDto availabilityDto, Availability availableDays) {
        availableDays.setAvailableDays(availabilityDto.getAvailableDays());
        return availabilityRepository.save(availableDays);
    }

    private Availability createAvailability(AvailabilityDto availabilityDto, User currentUser) {
        return availabilityRepository.save(manualAvailabilityMapper.dtoToModel(availabilityDto, currentUser));
    }


//    public Set<AvailabilityDto> addAvailableDay(String user, AvailabilityDto availabilityDto) {
//
//    }
}
