package get2gether.event;

import get2gether.model.Invite;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class InviteStatusChangedEvent  extends ApplicationEvent {

    private final Invite updatedInvite;
    private final Boolean accepted;

    public InviteStatusChangedEvent(Object source, Invite updatedInvite, Boolean accepted) {
        super(source);
        this.updatedInvite = updatedInvite;
        this.accepted = accepted;
    }


}
