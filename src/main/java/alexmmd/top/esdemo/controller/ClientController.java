package alexmmd.top.esdemo.controller;

import alexmmd.top.esdemo.domain.Hero;
import alexmmd.top.esdemo.domain.OrderInfo;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.core.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

/**
 * @author 汪永晖
 * @date 2021/8/24 18:42
 */
@RestController
@RequestMapping("/client")
@Slf4j
public class ClientController {

    @Resource(name = "remoteHighLevelClient")
    private RestHighLevelClient client;

    @GetMapping("/search")
    public void search() throws IOException {
        SearchRequest searchRequest = new SearchRequest("hero");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("birthday", "公元18");
        matchQueryBuilder.fuzziness(Fuzziness.AUTO);
        matchQueryBuilder.prefixLength(3);
        matchQueryBuilder.maxExpansions(10);
//        searchSourceBuilder.query(QueryBuilders.termQuery("birthday", "公元181年"));
        searchSourceBuilder.query(matchQueryBuilder);
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(1);
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
            // do something with the SearchHit
            log.info(hit.toString());
        }
    }

    @PostMapping("/add")
    public String add(@RequestBody OrderInfo orderInfo) throws IOException {
        IndexRequest request = new IndexRequest("posts");
        request.id("1");
        String jsonString = JSONObject.toJSONString(orderInfo);
        log.info(jsonString);
        request.source(jsonString, XContentType.JSON);
        IndexResponse indexResponse = client.index(request, RequestOptions.DEFAULT);
        log.info(indexResponse.toString());
        return DocWriteResponse.Result.CREATED.equals(indexResponse.getResult()) ? "SUCCESS" : "FAILURE";
    }

    @GetMapping("/query")
    public OrderInfo query() throws IOException {
        GetRequest getRequest = new GetRequest("posts", "1");
        GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);
        String index = getResponse.getIndex();
        String id = getResponse.getId();
        if (getResponse.isExists()) {
            long version = getResponse.getVersion();
            String sourceAsString = getResponse.getSourceAsString();
            OrderInfo orderInfo = JSONObject.parseObject(sourceAsString, OrderInfo.class);
            log.info("index==={}, id==={}, version==={}", index, id, version);
            log.info(sourceAsString);
            log.info(orderInfo.toString());
            return orderInfo;
        } else {
            return null;
        }
    }

    @DeleteMapping("/delete/{id}")
    public String delete(@PathVariable String id) throws IOException {
        DeleteRequest request = new DeleteRequest("posts", id);
        DeleteResponse deleteResponse = client.delete(
                request, RequestOptions.DEFAULT);
        return DocWriteResponse.Result.DELETED.equals(deleteResponse.getResult()) ? "SUCCESS" : "FAILURE";
    }

    @PostMapping("/bulk")
    public String bulk() throws IOException {
        BulkRequest request = new BulkRequest();
        request.add(new IndexRequest("hero").id("2")
                .source(XContentType.JSON, "id", "2", "name", "刘备", "country", "蜀", "birthday", "公元161年", "longevity", 61));
        request.add(new IndexRequest("hero").id("3")
                .source(XContentType.JSON, "id", "3", "name", "孙权", "country", "吴", "birthday", "公元182年", "longevity", 61));
        request.add(new IndexRequest("hero").id("4")
                .source(XContentType.JSON, "id", "4", "name", "诸葛亮", "country", "蜀", "birthday", "公元181年", "longevity", 53));
        request.add(new IndexRequest("hero").id("5")
                .source(XContentType.JSON, "id", "5", "name", "司马懿", "country", "魏", "birthday", "公元179年", "longevity", 72));
        request.add(new IndexRequest("hero").id("6")
                .source(XContentType.JSON, "id", "6", "name", "荀彧", "country", "魏", "birthday", "公元163年", "longevity", 49));
        request.add(new IndexRequest("hero").id("7")
                .source(XContentType.JSON, "id", "7", "name", "关羽", "country", "蜀", "birthday", "公元160年", "longevity", 60));
        request.add(new IndexRequest("hero").id("8")
                .source(XContentType.JSON, "id", "8", "name", "周瑜", "country", "吴", "birthday", "公元175年", "longevity", 35));
        BulkResponse bulkResponse = client.bulk(request, RequestOptions.DEFAULT);
        log.info(bulkResponse.getItems().toString());
        return "SUCCESS";
    }

    @GetMapping("/boolQueryTest")
    public List<Hero> boolQueryTest() {
        SearchRequest request = new SearchRequest("hero");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder.must(termQuery("country", "魏"));
        boolQueryBuilder.must(rangeQuery("longevity").gte(50));
        sourceBuilder.query(boolQueryBuilder);
        sourceBuilder.from(0).size(10);
        sourceBuilder.query(boolQueryBuilder);
        sourceBuilder.sort("longevity", SortOrder.DESC);
        request.source(sourceBuilder);
        SearchResponse response = null;
        try {
            response = client.search(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("Query by Condition execution failed: {}", e.getMessage(), e);
        }
        assert response != null;
        log.info(String.valueOf(response.getShardFailures().length));
        SearchHit[] hits = response.getHits().getHits();
        List<Hero> herosList = new ArrayList<>(hits.length);
        for (SearchHit hit : hits) {
            herosList.add(JSON.parseObject(hit.getSourceAsString(), Hero.class));
        }
        log.info("print info: {}, size: {}", herosList.toString(), herosList.size());
        return herosList;
    }
}
