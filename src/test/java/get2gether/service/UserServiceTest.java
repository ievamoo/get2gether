package get2gether.service;

import get2gether.TestData;
import get2gether.dto.UserDto;
import get2gether.exception.ResourceNotFoundException;
import get2gether.model.ResourceType;
import get2gether.mapper.UserMapper;
import get2gether.model.User;
import get2gether.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService testUserService;

    private final User user = TestData.getTestUser();
    private final UserDto userDto = TestData.getTestUserDto();

    @Test
    void getUserByUsername_shouldReturnUserDtoWhenUserExists() {
        when(userRepository.findByUsername("test@gmail.com")).thenReturn(Optional.of(user));
        when(userMapper.modelToDtoOnGetUser(user)).thenReturn(userDto);

        var result = testUserService.getUserByUsername("test@gmail.com");

        assertNotNull(result);
        assertEquals("test@gmail.com", result.getUsername());
        assertEquals("TestName", result.getFirstName());
    }


    @Test
    void getUserByUsername_shouldThrowExceptionIfUserNotFound() {
        var username = "user@mail.com";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> testUserService.getUserByUsername(username));
    }

    @Test
    void updateCurrentUser_shouldUpdateUserWhenUserExists() {
        var savedUser = TestData.getSavedUser();
        var savedUserDto = TestData.getSavedUserDto();

        when(userRepository.findByUsername("test@gmail.com")).thenReturn(Optional.of(user));
        doNothing().when(userMapper).updateCurrentUser(any(UserDto.class), any(User.class));
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.modelToDtoOnGetUser(savedUser)).thenReturn(savedUserDto);

        var result = testUserService.updateCurrentUser("test@gmail.com", savedUserDto);

        assertNotNull(result);
        assertEquals("UpdatedTestName", result.getFirstName());
        assertEquals("UpdatedTestLastName", result.getLastName());
        assertEquals("test@gmail.com", result.getUsername());
    }


    @Test
    void deleteUser_shouldDeleteUser() {
        when(userRepository.findByUsername("test@gmail.com")).thenReturn(Optional.of(user));

        testUserService.deleteUser(user.getUsername());

        verify(userRepository).delete(user);
    }

    @Test
    void getAllUsers_shouldReturnListOfUsersWhenUsersExist() {
        var users = List.of(user);
        when(userRepository.findAll()).thenReturn(users);
        when(userMapper.modelToDtoOnGroupCreate(user)).thenReturn(userDto);

        var result = testUserService.getAllUsers();

        assertEquals(1, result.size());
        assertEquals("test@gmail.com", result.get(0).getUsername());
    }


    @Test
    void getAllUsers_shouldReturnEmptyListWhenNoUsers() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        var result = testUserService.getAllUsers();

        assertEquals(0, result.size());
    }

    @Test
    void updateAvailableDays_shouldUpdateAndReturnUpdatedDays() {
        Set<LocalDate> newDays = Set.of(LocalDate.of(2025, 5, 17), LocalDate.of(2025, 5, 18));

        user.setAvailableDays(new HashSet<>());

        when(userRepository.findByUsername("test@gmail.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Set<LocalDate> result = testUserService.updateAvailableDays("test@gmail.com", newDays);

        assertEquals(newDays, result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void getAvailableDays_shouldReturnExistingDays() {
        Set<LocalDate> existingDays = Set.of(LocalDate.of(2025, 5, 20));
        user.setAvailableDays(existingDays);

        when(userRepository.findByUsername("test@gmail.com")).thenReturn(Optional.of(user));

        Set<LocalDate> result = testUserService.getAvailableDays("test@gmail.com");

        assertEquals(existingDays, result);
    }

    @Test
    void getUserFromDb_shouldReturnUserWhenUserExists() {
        when(userRepository.findByUsername("test@gmail.com")).thenReturn(Optional.of(user));

        var result = testUserService.getUserFromDb("test@gmail.com");

        assertNotNull(result);
        assertEquals("test@gmail.com", result.getUsername());
        assertEquals("TestName", result.getFirstName());
        assertEquals("TestLastName", result.getLastName());
    }

    @Test
    void getUserFromDb_shouldThrowExceptionWhenUserNotFound() {
        String username = "nonexistent@gmail.com";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        var exception = assertThrows(ResourceNotFoundException.class,
                () -> testUserService.getUserFromDb(username));
        
        assertEquals("USER not found with username: " + username, exception.getMessage());
    }
}
