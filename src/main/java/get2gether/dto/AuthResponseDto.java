package get2gether.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer object for authentication responses.
 * Contains JWT token for authenticated sessions.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponseDto {

    @JsonProperty("jwt")
    private String jwt;
}
