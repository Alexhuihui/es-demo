package alexmmd.top.esdemo.init;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 汪永晖
 * @date 2021/8/24 17:58
 */
@Configuration
public class EsConfig {

    @Value("${elasticsearch.host:localhost}")
    public String host;

    /**
     * 之前使用transport的接口的时候是9300端口，现在使用HighLevelClient则是9200端口
     */
    @Value("${elasticsearch.port:9200}")
    public int port;

    public static final String SCHEME = "http";

    @Value("${elasticsearch.username:admin}")
    public String username;

    @Value("${elasticsearch.authenticationPassword:123}")
    public String authenticationPassword;

    @Bean(name = "remoteHighLevelClient")
    public RestHighLevelClient restHighLevelClient() {
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username,
                authenticationPassword));
        RestClientBuilder builder = RestClient.builder(new HttpHost(host, port, SCHEME));
//                setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder
//                        .setDefaultCredentialsProvider(credentialsProvider));
        return new RestHighLevelClient(builder);
    }
}

