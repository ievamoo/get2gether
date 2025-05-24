package get2gether.event;

import get2gether.model.Group;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.Set;

/**
 * Event that is published when a new group is created.
 * Contains information about the created group and the usernames of users invited to join.
 */
@Getter
public class GroupCreatedEvent extends ApplicationEvent {

    private final Group group;
    private final Set<String> invitedUsernames;

    public GroupCreatedEvent(Object source, Group group, Set<String> invitedUsernames) {
        super(source);
        this.group = group;
        this.invitedUsernames = invitedUsernames;
    }

}
