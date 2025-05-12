package get2gether.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class InviteSocketController {

    @MessageMapping("/presence-ping")
    public void handlePresence() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
             Object principal = authentication.getPrincipal();
            String username = ((UserDetails) principal).getUsername();
        }
//        if (principal == null) {
//            log.warn("Presence ping received with null Principal.");
//            return;
//        }
//        String username = principal.getName();
    }



}
