package get2gether.event;

import get2gether.model.Invite;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event that is published when an invite's status changes (accepted or declined).
 * Contains information about the updated invite and its new status.
 */
@Getter
public class InviteStatusChangedEvent extends ApplicationEvent {

    private final Invite updatedInvite;
    private final Boolean accepted;

    public InviteStatusChangedEvent(Object source, Invite updatedInvite, Boolean accepted) {
        super(source);
        this.updatedInvite = updatedInvite;
        this.accepted = accepted;
    }

}
