package com.shop.fashion.services;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.function.Function;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.shop.fashion.entities.User;
import com.shop.fashion.exceptions.CustomException;
import com.shop.fashion.exceptions.ErrorCode;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    public String extractId(String token) {
        try {

            int i = token.lastIndexOf('.');
            String withoutSignature = token.substring(0, i + 1);
            Jwt<Header, Claims> untrusted = Jwts.parserBuilder().build().parseClaimsJwt(withoutSignature);
            return untrusted.getBody().getSubject();
        } catch (MalformedJwtException e) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver, String key) {
        final Claims claims = extractAllClaims(token, key);
        return claimsResolver.apply(claims);
    }

    public String generateToken(User user, String key, long jwtExpiration) {
        return buildToken(new HashMap<>(), user, key, jwtExpiration);
    }

    private String buildToken(
            Map<String, Object> extraClaims,
            User user,
            String key,
            long expiration) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("scope", buildScope(user));
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(user.getId())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .addClaims(claims)
                .signWith(getSignInKey(key), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, String key) {
        // String id = extractIdUser(token, key);
        return !isTokenExpired(token, key);
    }

    public String extractIdUser(String token, String key) {
        return extractClaim(token, Claims::getSubject, key);
    }

    private boolean isTokenExpired(String token, String key) {
        return extractExpiration(token, key).before(new Date());
    }

    private Date extractExpiration(String token, String key) {
        return extractClaim(token, Claims::getExpiration, key);
    }

    private Claims extractAllClaims(String token, String key) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey(key))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey(String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");

        if (!CollectionUtils.isEmpty(user.getRoles()))
            user.getRoles().forEach(role -> {
                stringJoiner.add(role.getName());
            });

        return stringJoiner.toString();
    }
}
