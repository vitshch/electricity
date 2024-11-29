package dev.vitalish.electricity.config;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.http.HttpClient;

@Configuration
public class AppConfig {

    @Bean
    public JsonFactory jsonGoogleFactory() {
        return GsonFactory.getDefaultInstance();
    }

    @Bean
    public HttpClient httpClient(
            @Value("${proxy.host}") String proxyHost,
            @Value("${proxy.port}") Integer proxyPort
    ) {
        HttpClient.Builder httpClient = HttpClient.newBuilder();
        if (StringUtils.hasLength(proxyHost)) {
            InetSocketAddress proxy = new InetSocketAddress(proxyHost, proxyPort);
            httpClient.proxy(ProxySelector.of(proxy));
        } else {
            httpClient.proxy(ProxySelector.getDefault());
        }
        return httpClient.build();
    }
}
