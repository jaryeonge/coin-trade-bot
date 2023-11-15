package com.jr.coin.trade.bot.config;

import com.jr.coin.trade.bot.util.lambda.HttpClientThrowingConsumer;
import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.reactivestreams.Publisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.ClientCodecConfigurer;
import org.springframework.http.codec.LoggingCodecSupport;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.util.MimeType;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.util.annotation.Nullable;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Slf4j
@Configuration
public class WebClientConfig {

    private static final int CONNECTION_TIMEOUT = 500;
    private static final int READ_TIMEOUT = 60000;
    private static final int WRITE_TIMEOUT = 60000;

    @Bean
    public static WebClient webClient() {
        Consumer<ClientCodecConfigurer> consumer = configurer ->
                configurer
                        .defaultCodecs()
                        .jackson2JsonEncoder(new Jackson2JsonEncoder() {
                            @Override
                            public Flux<DataBuffer> encode(Publisher<?> inputStream, DataBufferFactory bufferFactory, ResolvableType elementType, @Nullable MimeType mimeType, @Nullable Map<String, Object> hints) {
                                return super.encode(inputStream, bufferFactory, elementType, mimeType, hints)
                                        .doOnNext(dataBuffer -> {
                                                    log.info("\n[Request Body] \n{} " +
                                                                    "\n====================================================================\n"
                                                            , RegExUtils.replaceAll(StandardCharsets.UTF_8.decode(dataBuffer.asByteBuffer()).toString(), ",", ",\n"));
                                                }
                                        );
                            }
                        });

        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                .codecs(consumer)
                .build();

        exchangeStrategies
                .messageWriters().stream()
                .filter(LoggingCodecSupport.class::isInstance)
                .forEach(w -> ((LoggingCodecSupport) w).setEnableLoggingRequestDetails(true));

        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECTION_TIMEOUT)
                .responseTimeout(Duration.ofMillis(CONNECTION_TIMEOUT + READ_TIMEOUT))
                .doOnConnected(c -> c.addHandlerLast(new ReadTimeoutHandler(READ_TIMEOUT, TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(WRITE_TIMEOUT, TimeUnit.MILLISECONDS)))
                .secure(
                        HttpClientThrowingConsumer.unchecked(
                                sslContextSpec -> sslContextSpec.sslContext(
                                        SslContextBuilder.forClient()
                                                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                                                .build()
                                )
                        )
                );

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(exchangeStrategies)
                .filter(ExchangeFilterFunction.ofRequestProcessor(
                        clientRequest -> {
                            String headers = headerToString(clientRequest.headers());
                            log.info("\n============================API REQUEST===========================\n" +
                                            "[Request URL] : {} {}\n" +
                                            "[Request Header] : \n {}"
                                    , clientRequest.method(), clientRequest.url()
                                    , headers);

                            return Mono.just(clientRequest);
                        }
                ))
                .filter(ExchangeFilterFunction.ofResponseProcessor(
                        clientResponse -> {
                            String headers = headerToString(clientResponse.headers().asHttpHeaders());
                            log.info("\n============================API RESPONSE===========================\n" +
                                            "[Response Header] : \n {}"
                                    , headers);

                            return Mono.just(clientResponse);
                        }
                ))
                .build();
    }

    private static String headerToString(HttpHeaders httpHeaders) {
        String result = StringUtils.EMPTY;

        String[] temp = httpHeaders
                .entrySet()
                .stream()
                .map(e -> e.getKey() + " : " + e.getValue() + "\n")
                .toArray(String[]::new);

        result = StringUtils.replaceEach(Arrays.toString(temp), new String[]{"[", "]", ","}, new String[]{"", "", ""});

        return result;
    }
}
