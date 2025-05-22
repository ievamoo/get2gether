package get2gether.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer object for event participation status.
 * Indicates whether a user is attending an event.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventStatusDto {
    private Boolean isGoing;
}
