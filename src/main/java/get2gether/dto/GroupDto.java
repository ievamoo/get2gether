package get2gether.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Data transfer object for group information.
 * Contains group details, members, events, messages, and availability information.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Accessors(chain = true)
public class GroupDto {
    private Long id;
    private String name;
    private UserDto admin;
    private Set<UserDto> members;
    private Map<LocalDate, Set<UserDto>> groupAvailability;
    private List<EventDto> events;
    private List<MessageDto> messages;
    private String groupColor;
    private Set<String> invitedUsernames;
}
