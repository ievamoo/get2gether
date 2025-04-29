package get2gether.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class GroupDto {
    private Long id;
    private String name;
    private UserDto admin;
    private Set<UserDto> members;
    private List<EventDto> events;
    private List<MessageDto> messages;
}
