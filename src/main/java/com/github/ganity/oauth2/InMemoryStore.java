package com.github.ganity.oauth2;

import java.util.concurrent.ConcurrentHashMap;

public class InMemoryStore {
    protected final ConcurrentHashMap<String, String> authorizationCodeStore = new ConcurrentHashMap<>();

    protected void store(String key, String value) {
        this.authorizationCodeStore.put(key, value);
    }

    public String remove(String key) {
        String auth = this.authorizationCodeStore.remove(key);
        return auth;
    }
}
