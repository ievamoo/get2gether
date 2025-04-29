package get2gether.dto;

import get2gether.model.Type;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageDto {
    private Long id;
    private Type type;
    private Long typeId;
    private Long senderId;
    private String message;
    private LocalDateTime createdAt;
}


