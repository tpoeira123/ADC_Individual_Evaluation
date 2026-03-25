package pt.unl.fct.di.adc.webapp.util;


import java.util.UUID;

public class AuthToken {

    public static final long EXPIRATION_TIME = 1000*60*60*2; // 2h
    
    private String tokenId;
    private String userId;
    private String role;
    private long issuedAt;
    private long expiresAt;

    public AuthToken() { }

    public AuthToken(String userId,  String role) {
        this.userId = userId;
        this.role = role;
        this.tokenId = UUID.randomUUID().toString();
        this.issuedAt = System.currentTimeMillis();
        this.expiresAt = this.issuedAt + EXPIRATION_TIME;
    }

    public String getTokenId() {
        return tokenId;
    }

    public String getUserId() {
        return userId;
    }

    public String getRole() {
        return role;
    }

    public long getIssuedAt() {
        return issuedAt;
    }
    public long getExpiresAt() {
        return expiresAt;
    }

    
}
