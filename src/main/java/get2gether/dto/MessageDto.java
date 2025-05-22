package get2gether.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data transfer object for group messages.
 * Contains message content and metadata (sender, timestamp).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageDto {
    private Long id;
    private Long groupId;
    private String senderUsername;
    private String message;
    private LocalDateTime createdAt;
}


