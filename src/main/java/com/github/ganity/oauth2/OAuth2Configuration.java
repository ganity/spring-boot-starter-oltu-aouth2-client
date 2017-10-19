package com.github.ganity.oauth2;

import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "oauth2", value = "enable", matchIfMissing = true)
@EnableConfigurationProperties(OAuthProperties.class)
@ComponentScan(basePackages = {"com.github.ganity.oauth2"})
public class OAuth2Configuration {

    @Autowired
    private OAuthProperties oAuthProperties;

    @Bean
    public AuthzCallback authzCallback() {
        AuthzCallback authzCallback = null;
        Class<? extends AuthzCallback> callbackClass = oAuthProperties.getAuthzCallbackClass();
        if (null != callbackClass) {
            try {
                authzCallback = callbackClass.newInstance();
            } catch (InstantiationException var2) {
                throw new BeanInstantiationException(callbackClass, "Is it an abstract class?", var2);
            } catch (IllegalAccessException var3) {
                throw new BeanInstantiationException(callbackClass, "Is the constructor accessible?", var3);
            }
        }
        if (null == authzCallback) {
            authzCallback = new NoneSimpleAuthzCallback();
        }

        return authzCallback;
    }

    @Bean
    public InMemoryStore inMemoryStore() {
        return new InMemoryStore();
    }

}
