package com.github.ganity.oauth2;


import java.util.Map;

public class NoneSimpleAuthzCallback implements AuthzCallback {
    @Override
    public void callback(OAuth2AccessToken accessToken, Map<String, String> principal) {

    }
}
