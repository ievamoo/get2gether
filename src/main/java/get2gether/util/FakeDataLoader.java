//package get2gether.util;
//
//import get2gether.model.Role;
//import get2gether.model.User;
//import get2gether.repository.UserRepository;
//import jakarta.annotation.PostConstruct;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//
//@Component
//@RequiredArgsConstructor
//public class FakeDataLoader {
//
//    private final UserRepository userRepository;
//    private final PasswordEncoder passwordEncoder;
//
//    @PostConstruct
//    public void createAdmin() {
//        var admin = User.builder()
//                .username("admin@gmail.com")
//                .firstName("Admin")
//                .lastName("Admin")
//                .password(passwordEncoder.encode("admin123"))
//                .roles(List.of(Role.ADMIN))
//                .build();
//        userRepository.save(admin);
//    }
//}
