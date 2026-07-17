package TNB.SmsGateway.security;
import TNB.SmsGateway.entity.ApiKeyScope;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.UUID;

public class ApiKeyAuthentication extends AbstractAuthenticationToken {

    private final UUID userId;
    private final String apiKeyId;
    private final ApiKeyScope scope;

    public ApiKeyAuthentication(UUID userId, String apiKeyId, ApiKeyScope scope) {
        super(null);
        this.userId = userId;
        this.apiKeyId = apiKeyId;
        this.scope = scope;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return userId;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getApiKeyId() {
        return apiKeyId;
    }

    public ApiKeyScope getScope() {
        return scope;
    }
}