package get2gether.service;

import get2gether.dto.UserDto;
import get2gether.exception.ForbiddenActionException;
import get2gether.exception.ResourceNotFoundException;
import get2gether.mapper.UserMapper;
import get2gether.model.ResourceType;
import get2gether.model.Role;
import get2gether.model.User;
import get2gether.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Service responsible for managing user-related operations.
 * Handles user retrieval, updates, deletion, and availability management.
 * Provides functionality for both individual user operations and user listing.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    /**
     * Retrieves a user by their username and converts it to a DTO.
     *
     * @param username the username to search for
     * @return UserDto containing the user's information
     * @throws ResourceNotFoundException if the user is not found
     */
    public UserDto getUserByUsername(String username) {
        var matchingUser = getUserFromDb(username);
        return userMapper.modelToDtoOnGetUser(matchingUser);
    }

    /**
     * Updates the current user's information.
     * The method:
     * 1. Retrieves the existing user
     * 2. Updates the user's information using the provided DTO
     * 3. Saves the updated user
     * 4. Returns the updated user information as a DTO
     *
     * @param username the username of the user to update
     * @param updatedUserDto the updated user information
     * @return UserDto containing the updated user information
     * @throws ResourceNotFoundException if the user is not found
     */
    @Transactional
    public UserDto updateCurrentUser(String username, UserDto updatedUserDto) {
        var matchingUser = getUserFromDb(username);
        userMapper.updateCurrentUser(updatedUserDto, matchingUser);
        var savedUser = userRepository.save(matchingUser);
        return userMapper.modelToDtoOnGetUser(savedUser);
    }

    /**
     * Retrieves a user entity from the database by username.
     *
     * @param username the username to search for
     * @return the User entity
     * @throws ResourceNotFoundException if the user is not found
     */
    public User getUserFromDb(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(ResourceType.USER, "username: " + username));
    }

    /**
     * Deletes a user from the system.
     * The method prevents deletion if the user is an admin of any group.
     *
     * @param username the username of the user to delete
     * @throws ResourceNotFoundException if the user is not found
     * @throws ForbiddenActionException if the user is an admin of any group
     */
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

    /**
     * Retrieves all users with the USER role.
     * Returns a list of UserDto objects suitable for group creation.
     *
     * @return List of UserDto objects containing user information
     */
    public List<UserDto> getAllUsers() {
        var users = userRepository.findAll();
        return users.stream()
                .filter(user -> user.getRoles().contains(Role.USER))
                .map(userMapper::modelToDtoOnGroupCreate)
                .toList();
    }

    /**
     * Updates a user's available days.
     *
     * @param userName the username of the user to update
     * @param availableDays the set of dates the user is available
     * @return the updated set of available days
     * @throws ResourceNotFoundException if the user is not found
     */
    public Set<LocalDate> updateAvailableDays(String userName, Set<LocalDate> availableDays) {
        var matchingUser = getUserFromDb(userName);
        matchingUser.setAvailableDays(availableDays);
        var updatedUser = userRepository.save(matchingUser);
        return updatedUser.getAvailableDays();
    }

    /**
     * Retrieves a user's available days.
     *
     * @param userName the username of the user
     * @return the set of dates the user is available
     * @throws ResourceNotFoundException if the user is not found
     */
    public Set<LocalDate> getAvailableDays(String userName) {
        var matchingUser = getUserFromDb(userName);
        return matchingUser.getAvailableDays();
    }
}
