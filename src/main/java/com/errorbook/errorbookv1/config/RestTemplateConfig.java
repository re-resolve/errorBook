package com.errorbook.errorbookv1.config;/*
package com.example.errorBook.config;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {
    
    @Value("${rest.connectTimeout}")
    private int connectTimeout;
    
    @Value("${rest.readTimeout}")
    private int readTimeout;
    
    @Value("${rest.connectionRequestTimeout}")
    private int connectionRequestTimeout;
    
    */
/**
     * 高并发采用HttpClient连接池
     *//*

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate(httpRequestFactory());
    }
    
    
    @Bean
    public ClientHttpRequestFactory httpRequestFactory() {
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient());
    
        //设置客户端和服务端建立连接的超时时间
        requestFactory.setConnectTimeout(10000);
        //设置客户端从服务端读取数据的超时时间
        requestFactory.setReadTimeout(5000);
        //设置从连接池获取连接的超时时间，不宜过长
        requestFactory.setConnectionRequestTimeout(2000);
        //缓冲请求数据，默认为true。通过POST或者PUT大量发送数据时，建议将此更改为false，以免耗尽内存
        //requestFactory.setBufferRequestBody(false);
        
        return requestFactory;
        
    }
    
    */
/**
     * 使用 HttpComponentsClientHttpRequestFactory创建http请求（推荐）
     *//*

    
    @Bean
    public HttpClient httpClient() {
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", SSLConnectionSocketFactory.getSocketFactory())
                .build();
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(registry);
        //设置整个连接池最大连接数
        connectionManager.setMaxTotal(400);
        
        //路由是对maxTotal的细分
        connectionManager.setDefaultMaxPerRoute(100);
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(3000)  //返回数据的超时时间
                .setConnectTimeout(2000) //连接上服务器的超时时间
                .setConnectionRequestTimeout(1000) //从连接池中获取连接的超时时间
                .build();
        return HttpClientBuilder.create()
                //重试次数，默认是3次，没有开启
                .setRetryHandler(new DefaultHttpRequestRetryHandler(2, true))
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(connectionManager)
                .build();
    }

    
   
}
*/
