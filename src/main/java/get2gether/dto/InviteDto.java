package get2gether.dto;

import get2gether.model.Type;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

/**
 * Data transfer object for invitation information.
 * Contains details about group or event invitations and their status.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InviteDto {
    private Long id;
    private Type type;
    private Long typeId;
    private String typeName;
    private String groupName;
    private LocalDate eventDate;
    private String senderUsername;
    private Set<String> receiverUsernames;
    private Boolean accepted;
}
