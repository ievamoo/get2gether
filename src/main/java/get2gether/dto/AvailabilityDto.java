package get2gether.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

/**
 * Data transfer object for user availability information.
 * Contains a set of dates when a user is available.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AvailabilityDto {
    private Set<LocalDate> availableDays;
}
