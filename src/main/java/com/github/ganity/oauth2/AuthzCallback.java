package com.github.ganity.oauth2;

import java.util.Map;

public interface AuthzCallback {
    public static final String AUTH_ACCESS_TOKEN = "AUTH_ACCESS_TOKEN";
    public static final String PRINCIPAL = "principal";

    void callback(OAuth2AccessToken accessToken, Map<String, String> principal);
}
