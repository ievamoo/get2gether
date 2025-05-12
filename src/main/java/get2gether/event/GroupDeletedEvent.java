package get2gether.event;
import get2gether.model.Group;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;
@Getter
public class GroupDeletedEvent extends ApplicationEvent {

    private final Group deletedGroup;

    public GroupDeletedEvent(Object source, Group deletedGroup) {
        super(source);
        this.deletedGroup = deletedGroup;
    }

}
