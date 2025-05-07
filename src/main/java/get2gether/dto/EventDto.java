package get2gether.dto;

import jakarta.persistence.Access;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Accessors(chain = true)
public class EventDto {
    private Long id;
    private String name;
    private String hostUsername;
    private LocalDate date;
    private String description;
    private String groupName;
    private List<MessageDto> messages;
    private Set<UserDto> goingMembers;
}
