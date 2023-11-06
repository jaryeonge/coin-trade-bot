package com.jr.coin.trade.bot.helper;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.jr.coin.trade.bot.domain.response.UpbitErrorResponseDto;
import com.jr.coin.trade.bot.exception.UpbitApiException;
import com.jr.coin.trade.bot.util.Constants;
import com.jr.coin.trade.bot.util.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Slf4j
@Component
public class UpbitApiClient {

    @Value("${upbit.access-key}")
    private String accessKey;

    @Value("${upbit.secret-key}")
    private String secretKey;

    private static final String domain = "https://api.upbit.com/v1";

    private WebClient webClient;

    public UpbitApiClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public String createJwtToken(HashMap<String, String> params) throws NoSuchAlgorithmException {
        Algorithm algorithm = Algorithm.HMAC256(secretKey);

        if (params.entrySet().size() == 0) {
            return JWT.create()
                    .withClaim("access_key", accessKey)
                    .withClaim("nonce", UUID.randomUUID().toString())
                    .sign(algorithm);
        }

        ArrayList<String> queryElements = new ArrayList<>();
        for(Map.Entry<String, String> entity : params.entrySet()) {
            queryElements.add(entity.getKey() + "=" + entity.getValue());
        }

        String queryString = String.join("&", queryElements);

        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(queryString.getBytes(StandardCharsets.UTF_8));

        String queryHash = String.format("%0128x", new BigInteger(1, md.digest()));


        return JWT.create()
                .withClaim("access_key", accessKey)
                .withClaim("nonce", UUID.randomUUID().toString())
                .withClaim("query_hash", queryHash)
                .withClaim("query_hash_alg", "SHA512")
                .sign(algorithm);
    }

    public <T> T requestGetToExchange(Class<T> responseClass, String path, HashMap<String, String> params) {
        webClient = makeHeader();

        Mono<T> result = webClient.mutate()
                .build()
                .get()
                .uri(u -> u.path(path).build())
                .headers(httpHeaders -> {
                    try {
                        httpHeaders.setBearerAuth(createJwtToken(params));
                    } catch (NoSuchAlgorithmException e) {
                        throw new RuntimeException(e);
                    }
                })
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleUpbitApiError)
                .bodyToMono(responseClass)
                .contextWrite(context -> context.put(Constants.EXECUTION_TIME_MAP_KEY, System.currentTimeMillis()));
        return result.block();
    }

    public <T> T requestPostToExchange(Class<T> responseClass, String path, HashMap<String, String> params) {
        webClient = makeHeader();

        Mono<T> result = webClient.mutate()
                .build()
                .post()
                .uri(u -> u.path(path).build())
                .headers(httpHeaders -> {
                    try {
                        httpHeaders.setBearerAuth(createJwtToken(params));
                    } catch (NoSuchAlgorithmException e) {
                        throw new RuntimeException(e);
                    }
                })
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleUpbitApiError)
                .bodyToMono(responseClass)
                .contextWrite(context -> context.put(Constants.EXECUTION_TIME_MAP_KEY, System.currentTimeMillis()));
        return result.block();
    }

    public <T> T requestDeleteToExchange(Class<T> responseClass, String path, HashMap<String, String> params) {
        webClient = makeHeader();

        Mono<T> result = webClient.mutate()
                .build()
                .delete()
                .uri(u -> u.path(path).build())
                .headers(httpHeaders -> {
                    try {
                        httpHeaders.setBearerAuth(createJwtToken(params));
                    } catch (NoSuchAlgorithmException e) {
                        throw new RuntimeException(e);
                    }
                })
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleUpbitApiError)
                .bodyToMono(responseClass)
                .contextWrite(context -> context.put(Constants.EXECUTION_TIME_MAP_KEY, System.currentTimeMillis()));

        return result.block();
    }

    public <T> List<T> requestGetListToExchange(ParameterizedTypeReference<List<T>> responseClass, String path, HashMap<String, String> params) {
        webClient = makeHeader();

        Mono<List<T>> result = webClient.mutate()
                .build()
                .get()
                .uri(u -> u.path(path).build())
                .headers(httpHeaders -> {
                    try {
                        httpHeaders.setBearerAuth(createJwtToken(params));
                    } catch (NoSuchAlgorithmException e) {
                        throw new RuntimeException(e);
                    }
                })
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleUpbitApiError)
                .bodyToMono(responseClass)
                .contextWrite(context -> context.put(Constants.EXECUTION_TIME_MAP_KEY, System.currentTimeMillis()));

        return result.block();
    }

    public <T> T requestGetToQuotation(Class<T> responseClass, String path, HashMap<String, String> params) {
        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        params.forEach(multiValueMap::add);

        webClient = makeHeader();

        Mono<T> result = webClient.mutate()
                .build()
                .get()
                .uri(u -> u.path(path).queryParams(multiValueMap).build())
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleUpbitApiError)
                .bodyToMono(responseClass)
                .contextWrite(context -> context.put(Constants.EXECUTION_TIME_MAP_KEY, System.currentTimeMillis()));
        return result.block();
    }

    private WebClient makeHeader() {
        return webClient.mutate()
                .baseUrl(domain)
                .defaultHeaders(h -> {
                    h.setContentType(MediaType.APPLICATION_JSON);
                })
                .build();
    }

    private Mono<? extends Throwable> handleUpbitApiError(ClientResponse response) {
        response.bodyToMono(String.class)
                .publishOn(Schedulers.boundedElastic())
                .subscribe(body -> log.info("\n[Response Body]\nError RESPONSE : {}", body));
        UpbitErrorResponseDto upbitErrorResponseDto = response.bodyToMono(UpbitErrorResponseDto.class).block();
        if (upbitErrorResponseDto == null) {
            throw UpbitApiException.create(ErrorCode.UPBIT_ERROR);
        } else {
            throw UpbitApiException.create(upbitErrorResponseDto);
        }
    }

}
