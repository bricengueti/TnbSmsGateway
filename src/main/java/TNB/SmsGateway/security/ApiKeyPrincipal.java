package TNB.SmsGateway.security;

import TNB.SmsGateway.entity.ApiKey;
import TNB.SmsGateway.entity.ApiKeyScope;
import TNB.SmsGateway.entity.RoutingMode;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

public class ApiKeyPrincipal implements UserDetails {

    private final UUID userId;
    private final UUID apiKeyId;
    private final RoutingMode routingMode;
    private final ApiKeyScope scope;

    public ApiKeyPrincipal(ApiKey apiKey) {
        this.userId = apiKey.getUser().getId();
        this.apiKeyId = apiKey.getId();
        this.routingMode = apiKey.getRoutingMode();
        this.scope = apiKey.getScope();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return userId.toString();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getApiKeyId() {
        return apiKeyId;
    }

    public RoutingMode getRoutingMode() {
        return routingMode;
    }

    public ApiKeyScope getScope() {
        return scope;
    }
}