package com.example.common.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @LoadBalanced // eureka에 등록된 서비스명을 사용해서 내부서비스 호출(내부통신)
    @Bean
    public RestTemplate restTemplate() {
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5000); // 서버에 연결을 시도할 때까지 기다리는 최대 시간
        requestFactory.setReadTimeout(3000); // 서버로부터 응답을 기다리는 최대 시간

        return new RestTemplate(requestFactory);
    }
}
