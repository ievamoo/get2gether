package get2gether.event.listener;

import get2gether.dto.InviteDto;
import get2gether.event.GroupCreatedEvent;
import get2gether.model.Type;
import get2gether.repository.InviteRepository;
import get2gether.repository.UserRepository;
import get2gether.service.InviteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class GroupCreationListener {

    private final InviteService inviteService;

    //naujai sukurta grupe, reikia issiusti invites pasirinktiems memberiams, grupes adminas = siuntejas

    @EventListener
    public void sendInvitesToSelectedUsers(GroupCreatedEvent event) {
        inviteService.createInvitesOnGroupCreation(event.getGroup(), event.getInvitedUsernames());
    }

}
