package get2gether.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents a message in the Get2Gather application.
 * Messages are used for communication within groups.
 * Each message is associated with a specific group and includes
 * the sender's username, message content, and timestamp.
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    @JsonManagedReference
    private Group group;

    private String senderUsername;

    private String message;

    private LocalDateTime createdAt;
}
