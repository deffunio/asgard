package io.deffun.asgard.mn;

import io.deffun.asgard.core.AuthorizationSecurity;
import io.deffun.asgard.core.AuthorizeFieldVisibility;
import io.micronaut.security.utils.SecurityService;

public final class MicronautComponentsFactory {
    private MicronautComponentsFactory() {
    }

    public static AuthorizeFieldVisibility authorizeFieldVisibility(SecurityService securityService) {
        return new AuthorizeFieldVisibility(new AuthorizationSecurity() {
            @Override
            public boolean isAuthn() {
                return securityService.isAuthenticated();
            }

            @Override
            public boolean hasRole(String role) {
                return securityService.hasRole(role);
            }
        });
    }
}
