package get2gether.dto;

import get2gether.model.Type;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

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
    private String receiverUsername;
    private Boolean accepted;
}
