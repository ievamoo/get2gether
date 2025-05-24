package get2gether.event;

import get2gether.model.Group;
import get2gether.model.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event that is published when a user leaves a group.
 * Contains information about the group that was left and the user who left it.
 */
@Getter
public class GroupLeaveEvent extends ApplicationEvent {

    private final Group leftGroup;
    private final User user;

    public GroupLeaveEvent(Object source, Group group, User user) {
        super(source);
        this.leftGroup = group;
        this.user = user;
    }
}
