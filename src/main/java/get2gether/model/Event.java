package get2gether.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDate;
import java.util.Set;

/**
 * Represents an event in the Get2Gather application.
 * They include details such as location, time, and participant information.
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@DynamicUpdate
@Accessors(chain = true)
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String hostUsername;

    private LocalDate date;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    @JsonManagedReference
    private Group group;

    @ManyToMany
    @JoinTable(
            name = "user_event",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonManagedReference
    @EqualsAndHashCode.Exclude
    private Set<User> goingMembers;

}
