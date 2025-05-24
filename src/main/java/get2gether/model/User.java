package get2gether.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Represents a user in the Get2Gather application.
 * This entity stores user information including authentication details,
 * personal information, and relationships with other entities like groups and events.
 * Users can have different roles and participate in various activities within the platform.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
@DynamicUpdate
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private List<Role> roles;

    @ManyToMany(mappedBy = "members")
    @JsonBackReference
    @EqualsAndHashCode.Exclude
    private Set<Group> groups;

    @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Invite> invitesReceived;

    @ElementCollection
    @CollectionTable(name = "availability_days", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "available_day")
    private Set<LocalDate> availableDays;

    @ManyToMany(mappedBy = "goingMembers")
    @JsonBackReference
    @EqualsAndHashCode.Exclude
    private List<Event> goingEvents;

}
