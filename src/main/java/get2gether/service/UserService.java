package get2gether.service;

import get2gether.dto.GroupDto;
import get2gether.dto.UserDto;
import get2gether.exception.UserNotFoundException;
import get2gether.manualMapper.ManualGroupMapper;
import get2gether.manualMapper.ManualUserMapper;
import get2gether.model.User;
import get2gether.repository.GroupRepository;
import get2gether.repository.UserRepository;
import get2gether.mapper.UserMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final GroupRepository groupRepository;
    private final ManualGroupMapper manualGroupMapper;
    private final ManualUserMapper manualUserMapper;


    public UserDto getUserByUsername(String username) {
        var matchingUser = getUserFromDb(username);
//        manualUserMapper.modelToDtoOnGroupCreate(matchingUser);
        return manualUserMapper.modelToDtoOnGetUser(matchingUser);
    }

    @Transactional
    public UserDto updateCurrentUser(String username, UserDto updatedUserDto) {
        var matchingUser = getUserFromDb(username);
        userMapper.updateUserProfile(updatedUserDto, matchingUser);
        var savedUser = userRepository.save(matchingUser);
        return userMapper.modelToDto(savedUser);
    }

    public User getUserFromDb(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found:" + username));
    }

    @Transactional
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
