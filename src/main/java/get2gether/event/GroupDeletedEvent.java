package get2gether.event;

import get2gether.model.Group;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event that is published when a group is deleted.
 * Contains information about the deleted group.
 */
@Getter
public class GroupDeletedEvent extends ApplicationEvent {

    private final Group deletedGroup;

    public GroupDeletedEvent(Object source, Group deletedGroup) {
        super(source);
        this.deletedGroup = deletedGroup;
    }

}
