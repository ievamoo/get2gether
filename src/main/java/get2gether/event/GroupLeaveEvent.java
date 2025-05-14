package get2gether.event;

import get2gether.model.Group;
import get2gether.model.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

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
