package get2gether.dto;

import get2gether.model.InviteStatus;
import get2gether.model.Type;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InviteDto {
    private Long id;
    private Type type;
    private Long typeId;
    private String typeName;
    private String senderUsername;
    private String receiverUsername;
    private InviteStatus status;
}
