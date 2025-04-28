package get2gether.service;

import get2gether.dto.UserDto;
import get2gether.model.User;
import get2gether.repository.UserRepository;
import get2gether.util.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;


    public UserDto getUserByUsername(String username) {
        var matchingUser = getUserFromDb(username);
        return userMapper.modelToDto(matchingUser);
    }

    public UserDto updateCurrentUser(String username, UserDto updatedUserDto) {
        var matchingUser = getUserFromDb(username);
        userMapper.updateUserProfile(updatedUserDto, matchingUser);
        var savedUser = userRepository.save(matchingUser);
        return userMapper.modelToDto(savedUser);
    }

    private User getUserFromDb(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found:" + username));
    }

    public void deleteUser(String username) {
        var matchingUser = getUserFromDb(username);
        userRepository.delete(matchingUser);
    }

    public List<UserDto> getAllUsers() {
        var users = userRepository.findAll();
        return users.stream()
                .map(userMapper::modelToDto)
                .toList();
    }

}
