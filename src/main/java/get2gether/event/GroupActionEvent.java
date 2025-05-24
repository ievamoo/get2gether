package get2gether.event;

import get2gether.enums.GroupAction;
import get2gether.model.Group;
import get2gether.model.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.Set;

/**
 * Event that is published when a group-related action occurs.
 * Contains information about the group and the action type.
 * For LEAVE action, also contains information about the user who left.
 * For CREATED action, also contains information about invited users.
 */
@Getter
public class GroupActionEvent extends ApplicationEvent {

    private final Group group;
    private final GroupAction action;
    private final User user;
    private final Set<String> invitedUsernames;

    public GroupActionEvent(Object source, Group group, GroupAction action) {
        super(source);
        this.group = group;
        this.action = action;
        this.user = null;
        this.invitedUsernames = null;
    }

    public GroupActionEvent(Object source, Group group, GroupAction action, User user) {
        super(source);
        this.group = group;
        this.action = action;
        this.user = user;
        this.invitedUsernames = null;
    }

    public GroupActionEvent(Object source, Group group, GroupAction action, Set<String> invitedUsernames) {
        super(source);
        this.group = group;
        this.action = action;
        this.user = null;
        this.invitedUsernames = invitedUsernames;
    }


} 