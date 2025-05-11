package get2gether.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class InviteSocketController {

    @MessageMapping("/presence-ping")
    public void handlePresence(Principal principal) {
        if (principal == null) {
            log.warn("Presence ping received with null Principal.");
            return;
        }
        String username = principal.getName();
    }



}
