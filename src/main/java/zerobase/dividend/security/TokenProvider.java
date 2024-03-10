package zerobase.dividend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import zerobase.dividend.service.MemberService;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

@Component
public class TokenProvider {
    private static final String KEY_ROLES = "roles";
    private static final long TOKEN_EXPIRE_TIME = 1000 * 60 * 60;
    private final MemberService memberService;
    private final SecretKey secretKey;
    public TokenProvider (MemberService memberService, @Value("${jwt.secret}") String secretKey) {
        this.memberService = memberService;
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);

    }

    public String generateToken(String username, List<String> roles) {

        Date now = new Date();
        Date expireDate = new Date(now.getTime() +TOKEN_EXPIRE_TIME);

        return Jwts.builder()
                .claim(KEY_ROLES, roles)
                .claim(Claims.SUBJECT, username)
                .issuedAt(now)
                .expiration(expireDate)
                .signWith(secretKey, Jwts.SIG.HS512)
                .compact();
    }
    public Authentication getAuthentication(String jwt) {
        UserDetails userDetails = this.memberService.loadUserByUsername(this.getUsername(jwt));
        return new UsernamePasswordAuthenticationToken(userDetails,"",userDetails.getAuthorities());
    }
    public String getUsername(String token) {
        return this.parseClaims(token).getSubject();
    }
    public boolean validateToken(String token) {
        if (!StringUtils.hasText(token)) {
            return false;
        }
        Claims claims = this.parseClaims(token);
        return !claims.getExpiration().before(new Date());
    }
    private Claims parseClaims(String token) {
        try {
            return Jwts.parser().setSigningKey(this.secretKey).build().parseSignedClaims(token).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }
}
