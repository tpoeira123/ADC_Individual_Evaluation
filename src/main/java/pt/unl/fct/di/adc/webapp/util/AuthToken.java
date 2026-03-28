package pt.unl.fct.di.adc.webapp.util;


import java.util.UUID;

public class AuthToken {

    public static final long EXPIRATION_TIME = 900;  // 15 min
    
    private String tokenId;
    private String username;
    private String role;
    private long issuedAt;
    private long expiresAt;

    public AuthToken() { }

    public AuthToken(String username, String role) {
        this.username = username;
        this.role = role;
        this.tokenId = UUID.randomUUID().toString();
        this.issuedAt = System.currentTimeMillis() / 1000;
        this.expiresAt = this.issuedAt + EXPIRATION_TIME;
    }

    public String getTokenId() {
        return tokenId;
    }
    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }

    public long getIssuedAt() {
        return issuedAt;
    }
    public void setIssuedAt(long issuedAt) {
        this.issuedAt = issuedAt;
    }

    public long getExpiresAt() {
        return expiresAt;
    }
    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }
}
