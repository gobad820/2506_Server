package com.example.demo.common.oauth;

import com.example.demo.src.user.model.GoogleOAuthToken;
import com.example.demo.src.user.model.GoogleUser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleOauth implements SocialOauth {

    //applications.yml 에서 value annotation을 통해서 값을 받아온다.
    @Value("${spring.OAuth2.google.url}")
    private String GOOGLE_SNS_URL;

    @Value("${spring.OAuth2.google.client-id}")
    private String GOOGLE_SNS_CLIENT_ID;

    @Value("${spring.OAuth2.google.callback-login-url}")
    private String GOOGLE_SNS_CALLBACK_LOGIN_URL;

    @Value("${spring.OAuth2.google.client-secret}")
    private String GOOGLE_SNS_CLIENT_SECRET;

    @Value("${spring.OAuth2.google.scope}")
    private String GOOGLE_DATA_ACCESS_SCOPE;

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Override
    public String getOauthRedirectURL() {

        Map<String, Object> params = new HashMap<>();
        params.put("scope", GOOGLE_DATA_ACCESS_SCOPE);
        params.put("response_type", "code");
        params.put("client_id", GOOGLE_SNS_CLIENT_ID);
        params.put("redirect_uri", GOOGLE_SNS_CALLBACK_LOGIN_URL);


        //parameter를 형식에 맞춰 구성해주는 함수
        String parameterString = params.entrySet().stream()
                .map(x -> x.getKey() + "=" + x.getValue())
                .collect(Collectors.joining("&"));
        String redirectURL = GOOGLE_SNS_URL + "?" + parameterString;
        log.info("redirectURL = ", redirectURL);

        return redirectURL;
        /*
         * https://accounts.google.com/o/oauth2/v2/auth?scope=profile&response_type=code
         * &client_id="할당받은 id"&redirect_uri="access token 처리")
         * 로 Redirect URL을 생성하는 로직을 구성
         * */
    }


    public ResponseEntity<String> requestAccessToken(String code) {
        String GOOGLE_TOKEN_REQUEST_URL = "https://oauth2.googleapis.com/token";
        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> params = new HashMap<>();
        params.put("code", code);
        params.put("client_id", GOOGLE_SNS_CLIENT_ID);
        params.put("client_secret", GOOGLE_SNS_CLIENT_SECRET);
        params.put("redirect_uri", GOOGLE_SNS_CALLBACK_LOGIN_URL);
        params.put("grant_type", "authorization_code");

        ResponseEntity<String> responseEntity=restTemplate.postForEntity(GOOGLE_TOKEN_REQUEST_URL,
                params,String.class);

        if(responseEntity.getStatusCode() == HttpStatus.OK){
            return responseEntity;
        }
        return null;
    }

    public GoogleOAuthToken getAccessToken(ResponseEntity<String> response) throws JsonProcessingException {
        log.info("response.getBody() = {}", response.getBody());

        GoogleOAuthToken googleOAuthToken= objectMapper.readValue(response.getBody(),GoogleOAuthToken.class);
        return googleOAuthToken;

    }

    public ResponseEntity<String> requestUserInfo(GoogleOAuthToken oAuthToken) {
        String GOOGLE_USERINFO_REQUEST_URL="https://www.googleapis.com/oauth2/v1/userinfo";

        //header에 accessToken을 담는다.
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization","Bearer "+oAuthToken.getAccess_token());

        //HttpEntity를 하나 생성해 헤더를 담아서 restTemplate으로 구글과 통신하게 된다.
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity(headers);
        ResponseEntity<String> response = restTemplate.exchange(GOOGLE_USERINFO_REQUEST_URL, HttpMethod.GET,request,String.class);

        log.info("response.getBody() = {}", response.getBody());

        return response;
    }

    public GoogleUser getUserInfo(ResponseEntity<String> userInfoRes) throws JsonProcessingException{
        GoogleUser googleUser = objectMapper.readValue(userInfoRes.getBody(),GoogleUser.class);
        return googleUser;
    }
}
