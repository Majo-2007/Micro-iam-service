package co.sena.iam.adapter.out.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** HU-IAM-010: config de firma/expiración del JWT. Secreto vía env, nunca hardcodeado. */
@ConfigurationProperties(prefix = "iam.jwt")
public class JwtProperties {

    /** HS256, mínimo 32 bytes. Configurar por IAM_JWT_SECRET en cada ambiente. */
    private String secret = "change-me-change-me-change-me-change-me";
    private long accessTokenTtlSeconds = 900; // 15 min (RF-IAM-01)
    private String issuer = "iam-service";

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
    public long getAccessTokenTtlSeconds() { return accessTokenTtlSeconds; }
    public void setAccessTokenTtlSeconds(long v) { this.accessTokenTtlSeconds = v; }
    public String getIssuer() { return issuer; }
    public void setIssuer(String issuer) { this.issuer = issuer; }
}
