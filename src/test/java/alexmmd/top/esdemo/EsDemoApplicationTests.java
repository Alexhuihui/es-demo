package alexmmd.top.esdemo;

import alexmmd.top.esdemo.domain.FastOrderDto;
import alexmmd.top.esdemo.service.EsService;
import cn.hutool.core.lang.Assert;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

@SpringBootTest(classes = EsDemoApplication.class)
@Slf4j
class EsDemoApplicationTests {

    private final static String NGINX_LOG_INDEX = "filebeat-7.15.2-2021.12.03-000001";

    @Resource(name = "remoteHighLevelClient")
    private RestHighLevelClient client;

    @Resource
    private EsService esService;

    @Test
    public void testIndex() {
        String add = esService.add(333, "17770848782", "es-demo", "amanda", "qaq");
        log.info(add);
    }

    @Test
    public void testSearch() {
        List<FastOrderDto> search = esService.search("es-demo", 1, 10);
        log.info(search.toString());
    }

    @Test
    void contextLoads() {
    }

}
