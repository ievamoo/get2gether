package get2gether.service;

import get2gether.TestData;
import get2gether.dto.UserDto;
import get2gether.model.Role;
import get2gether.model.User;
import get2gether.repository.UserRepository;
import get2gether.util.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService testUserService;

    private final User user = TestData.getTestUser();
    private final UserDto userDto = TestData.getTestUserDto();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getUserByUsername_shouldReturnUserDtoWhenUserExists() {
        when(userRepository.findByUsername("test@gmail.com")).thenReturn(Optional.of(user));
        when(userMapper.modelToDto(user)).thenReturn(userDto);

        var result = testUserService.getUserByUsername("test@gmail.com");

        assertEquals("test@gmail.com", result.getUsername());
        assertEquals("TestName", result.getFirstName());
    }

    @Test
    void getUserByUsername_shouldThrowExceptionIfUserNotFound() {
        var username = "user@mail.com";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> testUserService.getUserByUsername(username));
    }

    @Test
    void updateCurrentUser_shouldUpdateUserWhenUserExists() {
        var savedUser = TestData.getSavedUser();
        var savedUserDto = TestData.getSavedUserDto();

        when(userRepository.findByUsername("test@gmail.com")).thenReturn(Optional.ofNullable(user));
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.modelToDto(savedUser)).thenReturn(savedUserDto);

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
        when(userMapper.modelToDto(user)).thenReturn(userDto);

        var result = testUserService.getAllUsers();

        assertEquals(1, result.size());
        assertEquals("test@gmail.com", result.get(0).getUsername());
    }

    @Test
    void getAllUsers_shouldReturnEmptyListWhenNoUsers() {
        when(userRepository.findAll()).thenReturn(new ArrayList<>());

        var result = testUserService.getAllUsers();

        assertEquals(0, result.size());
    }

}