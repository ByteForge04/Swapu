import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtGen {
    public static void main(String[] args) {
        String SECRET_STRING = "SwapU_Secret_Key_For_Campus_Trading_Platform_Needs_To_Be_Long_Enough";
        SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET_STRING.getBytes(StandardCharsets.UTF_8));
        long EXPIRATION_TIME = 24 * 60 * 60 * 1000L;
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", 1L);
        claims.put("username", "admin");
        claims.put("role", 1);
        
        String token = Jwts.builder()
                .claims(claims)
                .subject("admin")
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY)
                .compact();
        System.out.println(token);
    }
}
