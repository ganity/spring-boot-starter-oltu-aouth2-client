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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthBearerClientRequest;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthAccessTokenResponse;
import org.apache.oltu.oauth2.client.response.OAuthAuthzResponse;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.client.response.OAuthResourceResponse;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.apache.oltu.oauth2.common.message.types.ResponseType;
import org.omg.CORBA.portable.ApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

/**
 * oauth2.0 client controller
 */
@Controller
public class AuthzController {

    @Autowired
    public InMemoryStore memoryStore;
    private Logger logger = LoggerFactory.getLogger(AuthzController.class);
    @Autowired
    private OAuthProperties oAuthProperties;
    @Autowired
    private AuthzCallback authzCallback;
    private RandomValueStringGenerator generator = new RandomValueStringGenerator();

    @RequestMapping("/authorize")
    public ModelAndView authorize() throws OAuthSystemException, IOException {
        logger.debug("start processing /authorize request");

        // 生成state并存储
        String state = generator.generate();
        memoryStore.store(state, "1");

        OAuthClientRequest request = OAuthClientRequest
                .authorizationLocation(oAuthProperties.getAuthzEndpoint())
                .setClientId(oAuthProperties.getClientId())
                .setRedirectURI(oAuthProperties.getRedirectUri())
                .setResponseType(ResponseType.CODE.toString())
                .setScope(oAuthProperties.getScope())
                .setState(state)
                .buildQueryMessage();

        return new ModelAndView(new RedirectView(request.getLocationUri()));
    }

    @RequestMapping(value = "/redirect", method = RequestMethod.GET)
    public ModelAndView handleRedirect(HttpServletRequest request) {
        logger.debug("creating OAuth authorization response wrapper (/redirect)");

        try {
            // Create the response wrapper
            OAuthAuthzResponse oar = null;
            oar = OAuthAuthzResponse.oauthCodeAuthzResponse(request);

            // Get Authorization Code
            String code = oar.getCode();

            //验证 state值
            String state = oar.getState();
            String v = memoryStore.remove(state);
            if (StringUtils.isEmpty(v) || !"1".equals(v)) {
                throw new OAuthSystemException("error state value [" + state + "]");
            }

            OAuth2AccessToken accessToken = getToken(code);

            //获取resource
            Map<String, String> principal = getResource(accessToken);

            //获取token成功回调方法
            authzCallback.callback(accessToken, principal);

            return new ModelAndView(new RedirectView(oAuthProperties.getSuccessUrl()));
        } catch (OAuthProblemException e) {
            logger.error("failed to create OAuth authorization response wrapper", e);
        } catch (OAuthSystemException e) {
            logger.error("OAuth authorization response OAuthSystemException", e);
        } catch (IOException e) {
            logger.error("OAuth authorization response IOException", e);
        } catch (ApplicationException e) {
            logger.error("OAuth authorization response ApplicationException", e);
        }

        return new ModelAndView(new RedirectView(oAuthProperties.getErrorRedirectUri()));

    }

    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    public ModelAndView handleLogout(HttpServletRequest httpServletRequest) {
        logger.debug("do oauth 2.0 logout (/logout)");

        try {
            // 本地退出
            OAuth2AccessToken accessToken = authzCallback.logout();
            if (null == accessToken) {
                return new ModelAndView(new RedirectView(oAuthProperties.getErrorRedirectUri()));
            }

            // auth server 退出
            OAuthClientRequest request = OAuthClientRequest
                    .tokenLocation(oAuthProperties.getTokenEndpoint() + "?access_token=" + accessToken.getAccessToken())
                    .setClientId(oAuthProperties.getClientId())
                    .setClientSecret(oAuthProperties.getClientSecret())
                    .setRedirectURI(oAuthProperties.getRedirectUri())
//                    .setCode(authzCode)
                    .setGrantType(GrantType.AUTHORIZATION_CODE)
                    .buildBodyMessage();
            //使用base64进行加密
            byte[] tokenByte = Base64.encodeBase64((oAuthProperties.getClientId() + ":" + oAuthProperties.getClientSecret()).getBytes());
//            //将加密的信息转换为string
//            String tokenStr = DataTypeChange.bytesSub2String(tokenByte, 0, tokenByte.length);
            String tokenStr = new String(tokenByte);
//            //Basic YFUDIBGDJHFK78HFJDHF==    token的格式
            String token = "Basic " + tokenStr;
//        * "Basic Y2xpZW50YXV0aGNvZGU6MTIzNDU2"
            //把认证信息发到header中
            request.setHeader("Authorization", token);

            //设置Cookie
            Cookie[] cookies = httpServletRequest.getCookies();
            for (Cookie c : cookies) {
                if ("SESSION".equals(c.getName())) {
                    String cookie = c.getValue();
                    request.addHeader("Cookie", "SESSION="+cookie);
                }
            }

            OAuthClient client = new OAuthClient(new URLConnectionClient());
//            String app = Utils.findCookieValue(req, "app");

            Class<? extends OAuthAccessTokenResponse> cl = OAuthJSONAccessTokenResponse.class;

            OAuthAccessTokenResponse oauthResponse = client.accessToken(request, "DELETE", cl);

            if (oauthResponse.getResponseCode() == 200) {
//                String resultStr = oauthResponse.getBody();
//                ObjectMapper mapper = new ObjectMapper();
//                Map<String, String> result = mapper.readValue(resultStr, Map.class);
                return new ModelAndView(new RedirectView(oAuthProperties.getLogoutRedirectUri()));
            } else {
                logger.error("Could not access resource: " + oauthResponse.getResponseCode() + " " + oauthResponse.getBody());
                return new ModelAndView(new RedirectView(oAuthProperties.getErrorRedirectUri()));
            }

        } catch (Exception e) {
            logger.error("Could not logout :", e);
        }
        return new ModelAndView(new RedirectView(oAuthProperties.getErrorRedirectUri()));
    }

    public OAuth2AccessToken getToken(String authzCode) throws OAuthSystemException, IOException, ApplicationException, OAuthProblemException {
        logger.debug("authorizing");


        OAuthClientRequest request = OAuthClientRequest
                .tokenLocation(oAuthProperties.getTokenEndpoint())
                .setClientId(oAuthProperties.getClientId())
                .setClientSecret(oAuthProperties.getClientSecret())
                .setRedirectURI(oAuthProperties.getRedirectUri())
                .setCode(authzCode)
                .setGrantType(GrantType.AUTHORIZATION_CODE)
                .buildBodyMessage();
        //使用base64进行加密
        byte[] tokenByte = Base64.encodeBase64((oAuthProperties.getClientId() + ":" + oAuthProperties.getClientSecret()).getBytes());
//            //将加密的信息转换为string
//            String tokenStr = DataTypeChange.bytesSub2String(tokenByte, 0, tokenByte.length);
        String tokenStr = new String(tokenByte);
//            //Basic YFUDIBGDJHFK78HFJDHF==    token的格式
        String token = "Basic " + tokenStr;
//        * "Basic Y2xpZW50YXV0aGNvZGU6MTIzNDU2"
        //把认证信息发到header中
        request.setHeader("Authorization", token);
        OAuthClient client = new OAuthClient(new URLConnectionClient());
//            String app = Utils.findCookieValue(req, "app");

        Class<? extends OAuthAccessTokenResponse> cl = OAuthJSONAccessTokenResponse.class;

        OAuthAccessTokenResponse oauthResponse = client.accessToken(request, cl);

        OAuth2AccessToken accessToken = new OAuth2AccessToken();
        accessToken.setAccessToken(oauthResponse.getAccessToken());
        accessToken.setExpiresIn(oauthResponse.getExpiresIn());
        accessToken.setRefreshToken(oauthResponse.getRefreshToken());
        accessToken.setTokenType(oauthResponse.getTokenType());
        accessToken.setScope(oauthResponse.getScope());
        return accessToken;
    }

    public Map<String, String> getResource(OAuth2AccessToken accessToken) throws OAuthSystemException, OAuthProblemException, IOException {

        logger.debug("start processing /get_resource request");

        OAuthClientRequest request = getoAuthClientRequest(accessToken);

        OAuthClient client = new OAuthClient(new URLConnectionClient());
        OAuthResourceResponse resourceResponse = client.resource(request, "GET", OAuthResourceResponse.class);

        if (resourceResponse.getResponseCode() == 200) {
            String resultStr = resourceResponse.getBody();
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> result = mapper.readValue(resultStr, Map.class);
            return result;
        } else {
            logger.error("Could not access resource: " + resourceResponse.getResponseCode() + " " + resourceResponse.getBody());
            throw new OAuthSystemException(resourceResponse.getBody());
        }

    }

    private OAuthClientRequest getoAuthClientRequest(OAuth2AccessToken accessToken) throws OAuthSystemException {
        OAuthClientRequest request = null;

        OAuthBearerClientRequest oAuthBearerClientRequest =
                new OAuthBearerClientRequest(oAuthProperties.getResourceUrl())
                        .setAccessToken(accessToken.getAccessToken());
        request = oAuthBearerClientRequest.buildQueryMessage();
//            request = oAuthBearerClientRequest.buildHeaderMessage();
//            request = oAuthBearerClientRequest.buildBodyMessage();
        return request;
    }

}
