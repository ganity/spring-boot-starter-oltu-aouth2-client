/**
 * Copyright 2010 Newcastle University
 * <p>
 * http://research.ncl.ac.uk/smart/
 * <p>
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.ganity.oauth2;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("oauth2")
public class OAuthProperties {

    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String authzEndpoint;
    private String tokenEndpoint;
    private String resourceUrl;
    private String scope;
    private String successUrl;
    private Class<? extends AuthzCallback> authzCallbackClass = null;
    private String errorRedirectUri;

    private String logoutRedirectUri;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public String getAuthzEndpoint() {
        return authzEndpoint;
    }

    public void setAuthzEndpoint(String authzEndpoint) {
        this.authzEndpoint = authzEndpoint;
    }

    public String getTokenEndpoint() {
        return tokenEndpoint;
    }

    public void setTokenEndpoint(String tokenEndpoint) {
        this.tokenEndpoint = tokenEndpoint;
    }

    public String getResourceUrl() {
        return resourceUrl;
    }

    public void setResourceUrl(String resourceUrl) {
        this.resourceUrl = resourceUrl;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getSuccessUrl() {
        return successUrl;
    }

    public void setSuccessUrl(String successUrl) {
        this.successUrl = successUrl;
    }

    public Class<? extends AuthzCallback> getAuthzCallbackClass() {
        return authzCallbackClass;
    }

    public void setAuthzCallbackClass(Class<? extends AuthzCallback> authzCallbackClass) {
        this.authzCallbackClass = authzCallbackClass;
    }

    public String getErrorRedirectUri() {
        return errorRedirectUri;
    }

    public void setErrorRedirectUri(String errorRedirectUri) {
        this.errorRedirectUri = errorRedirectUri;
    }

    public String getLogoutRedirectUri() {
        return logoutRedirectUri;
    }

    public void setLogoutRedirectUri(String logoutRedirectUri) {
        this.logoutRedirectUri = logoutRedirectUri;
    }
}
