package com.github.ganity.oauth2;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

public class NoneSimpleAuthzCallback implements AuthzCallback {
    private InMemoryStore memoryStore = new InMemoryStore();

    @Override
    public void callback(OAuth2AccessToken accessToken, Map<String, String> principal) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            memoryStore.store(AuthzCallback.AUTH_ACCESS_TOKEN, mapper.writeValueAsString(accessToken));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public OAuth2AccessToken logout() {
        String ack = memoryStore.remove(AuthzCallback.AUTH_ACCESS_TOKEN);
        ObjectMapper mapper = new ObjectMapper();
        try {
            OAuth2AccessToken accessToken = mapper.readValue(ack, OAuth2AccessToken.class);
            return accessToken;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
