package com.authBackendSpring.springAuth.serviceLayer.UsersService;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.authBackendSpring.springAuth.models.Users;
import com.authBackendSpring.springAuth.repository.UserRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.function.Function;

@Service
public class JwtService {   

    @Autowired
    private UserRepository userRepository;
    public static int tokenStatus;

    public JwtService(){}
   

    @Value("${KEY}")
    private String privateKey;

        public String generateRefreshToken(String id){
            //get claims
            Map<String,Object> claims=new HashMap<>();

            return Jwts.builder()
                .claims()
                .add(claims)
                .subject(id) 
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 7*24  *60*60*1000L))
                .and()
                .signWith(getKey())
                .compact();
        }

        public String generateAccessToken(String email){
            //get claims
            Map<String,Object> claims=new HashMap<>();

            return Jwts.builder()
                .claims()
                .add(claims)
                .subject(email) 
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 60 * 1000L))
                .and()
                .signWith(getKey())
                .compact();
        }

        public boolean validateRefreshToken(String refreshToken) {
            return isTokenExpired(refreshToken);
        }
    

        private SecretKey getKey() {
            byte[] keyBytes=Decoders.BASE64.decode(privateKey);
            return Keys.hmacShaKeyFor(keyBytes);
        }

        public String extractUserEmail(String token) {
        // extract the username from jwt token, here using email instead
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
    final String email = extractUserEmail(token);

    if (email.equals(userDetails.getUsername()) && !isTokenExpired(token)) {
        return true; 
    }
    return false;
}
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        System.out.println(extractClaim(token, Claims::getExpiration));
        return extractClaim(token, Claims::getExpiration);
    }

    //get the refresh token from the db
    public String getRefreshToken(String email){
       Optional<Users>userOptional=userRepository.findByEmail(email);
        if(userOptional.isPresent()){
            return userOptional.get().getRefreshToken();
        }
        return null;
    }

}
