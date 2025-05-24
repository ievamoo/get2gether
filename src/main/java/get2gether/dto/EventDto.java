package get2gether.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Data transfer object for event information.
 * Contains event details, host information, and related entities (participants).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Accessors(chain = true)
public class EventDto {
    private Long id;
    private String name;
    private String hostUsername;
    private String hostFullName;
    private LocalDate date;
    private String description;
    private String groupName;
    private Set<UserDto> goingMembers;
}
