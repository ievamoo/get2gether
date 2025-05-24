package get2gether.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer object for user registration requests.
 * Contains the necessary information to create a new user account.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterRequestDto {
    private String firstName;
    private String lastName;
    private String username;
    private String password;
}
