package get2gether.controller;

import get2gether.dto.AvailabilityDto;
import get2gether.service.AvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/availability")
@RequiredArgsConstructor
public class AvailabilityController {

    private final AvailabilityService availabilityService;
    @PutMapping
    public ResponseEntity<AvailabilityDto> addAvailableDays(Authentication authentication, @RequestBody final AvailabilityDto availabilityDto) {
        var userName = authentication.getName();
        return ResponseEntity.status(HttpStatus.OK).body(availabilityService.addAvailableDay(userName, availabilityDto));
    }


}
