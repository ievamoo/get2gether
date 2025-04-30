package get2gether.exception;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler({
            UsernameNotFoundException.class,
            EntityNotFoundException.class,
            EntityExistsException.class
    })
    public ResponseEntity<Map<String, Object>> handleExceptions(Exception ex) {
        if (ex instanceof UsernameNotFoundException || ex instanceof EntityNotFoundException) {
            return createErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
        } else if (ex instanceof  EntityExistsException) {
            return createErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.internalServerError().build();
    }

    private ResponseEntity<Map<String, Object>> createErrorResponse(String message, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("error", message);
        response.put("status", status.value());
        return ResponseEntity.status(status).body(response);
    }
}
