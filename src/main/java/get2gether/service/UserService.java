package get2gether.service;

import get2gether.dto.UserDto;
import get2gether.exception.ForbiddenActionException;
import get2gether.manualMapper.ManualGroupMapper;
import get2gether.manualMapper.ManualUserMapper;
import get2gether.mapper.UserMapper;
import get2gether.model.User;
import get2gether.repository.GroupRepository;
import get2gether.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

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
        boolean isAdminOfAnyGroup = matchingUser.getGroups().stream()
                .anyMatch(group -> group.getAdmin().getUsername().equals(username));
        if (isAdminOfAnyGroup) {
            throw new ForbiddenActionException("Cannot delete user: they are the admin of one or more groups.");
        }
        userRepository.delete(matchingUser);
    }

    public List<UserDto> getAllUsers() {
        var users = userRepository.findAll();
        return users.stream()
                .map(manualUserMapper::modelToDtoOnGroupCreate)
                .toList();
    }

    public Set<LocalDate> updateAvailableDays(String userName, Set<LocalDate> availableDays) {
        var matchingUser = getUserFromDb(userName);
        matchingUser.setAvailableDays(availableDays);
        var updatedUser = userRepository.save(matchingUser);
        return updatedUser.getAvailableDays();
    }

    public Set<LocalDate> getAvailableDays(String userName) {
        var matchingUser = getUserFromDb(userName);
        return matchingUser.getAvailableDays();
    }

    public boolean userExistsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
}
