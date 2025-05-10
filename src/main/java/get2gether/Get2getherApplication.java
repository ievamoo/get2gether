package get2gether;

import io.jsonwebtoken.security.Keys;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.util.Base64;

@SpringBootApplication
public class Get2getherApplication {

	public static void main(String[] args) {
		SpringApplication.run(Get2getherApplication.class, args);
	}
}
