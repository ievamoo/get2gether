package get2gether.model;

import get2gether.enums.Type;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Represents an invitation in the Get2Gather application.
 * Invites are used to request participation in groups or events.
 * They track the status of invitations and manage the relationship between
 * the inviter and invitee.
 */
@Entity
@Table(name = "invite", indexes = {
        @Index(name = "idx_receiver_type_typeId", columnList = "receiver_id, type, typeId"),
        @Index(name = "idx_type_typeId", columnList = "type, typeId")
})
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Accessors(chain = true)

public class Invite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Type type;

    @Column(nullable = false)
    private Long typeId;

    @Column(nullable = false)
    private String typeName;

    @Column(nullable = false)
    private String senderUsername;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    private User receiver;

}
