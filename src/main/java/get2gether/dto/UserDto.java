package get2gether.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Data transfer object for user information.
 * Contains user details, availability, and related entities (groups, invites, events).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {
    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private Set<LocalDate> availableDays;
    private Set<GroupDto> groups;
    private List<InviteDto> invitesReceived;
    private List<EventDto> goingEvents;
}

