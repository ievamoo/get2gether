package get2gether.dto;

import jakarta.persistence.Access;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.util.List;

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
    private Long groupId;
    private List<MessageDto> messages;
}
