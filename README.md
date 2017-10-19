# spring-boot-starter-swagger2
A spring boot starter with oltu as a aouth2 client
## How to build and install

1. git clone the code from github

2. run the command in your console with maven:

```
	mvn install
```

## How to use 

- import the config in you `pom.xml`

```xml
<dependency>
    <groupId>com.github.ganity</groupId>
    <artifactId>spring-boot-starter-oltu-aouth2-client</artifactId>
    <version>1.0.0</version>
</dependency>
```

- add the follow config in you `application.yml` or `application.properties` 

```shell
oauth2:
  client_id: clientauthcode
  client-secret: 123456
  redirect-uri: http://localhost:8080/redirect
  authz-endpoint: http://localhost:8510/oauth/authorize
  token-endpoint: http://localhost:8510/oauth/token
  resource-url: http://localhost:8516/api/user
  scope: read write
  success-url: /index
  authz-callback-class: com.example.myoltuoauth2startersimple.ShiroAuthzCallback
  error-redirect-uri: /error
```
- implements `AuthzCallback` 

such as `com.example.myoltuoauth2startersimple.ShiroAuthzCallback`

`AuthzCallback` will return the `OAuth2AccessToken` and use info `principal` in `Map`

- the simple [my-oltu-oauth2-starter-simple](https://github.com/ganity/my-oltu-oauth2-starter-simple.git)



