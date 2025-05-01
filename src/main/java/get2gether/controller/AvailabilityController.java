package get2gether.controller;

import get2gether.dto.AvailabilityDto;
import get2gether.service.AvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/availability")
@RequiredArgsConstructor
public class AvailabilityController {

    private final AvailabilityService availabilityService;

//    @PostMapping
//    public ResponseEntity<Set<AvailabilityDto>> addAvailableDay(Authentication authentication, @RequestBody final AvailabilityDto availabilityDto) {
//        var user = authentication.getName();
//        return ResponseEntity.status(HttpStatus.CREATED).body(availabilityService.addAvailableDay(user, availabilityDto));
//    }

}
