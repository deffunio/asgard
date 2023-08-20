package io.deffun.asgard.core;

public interface AuthorizationSecurity {
    boolean isAuthn();

    boolean hasRole(String role);
}
