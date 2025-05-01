package get2gether.service;

import get2gether.dto.AvailabilityDto;
import get2gether.repository.AvailabilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private final AvailabilityRepository availabilityRepository;

//    public Set<AvailabilityDto> addAvailableDay(String user, AvailabilityDto availabilityDto) {
//
//    }
}
