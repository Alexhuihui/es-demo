package alexmmd.top.esdemo.service.impl;

import alexmmd.top.esdemo.domain.FastOrderDto;
import alexmmd.top.esdemo.service.EsService;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Cancellable;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonMap;

/**
 * @author 汪永晖
 * @date 2021/8/25 17:01
 */
@Service
@Slf4j
public class EsServiceImpl implements EsService {

    private final static String INDEX = "express_fast_order";

    @Resource(name = "remoteHighLevelClient")
    private RestHighLevelClient client;

    @Override
    public List<FastOrderDto> search(String keyword, Integer pageNum, Integer pageSize) {
        log.info("查找关键字{}, 页码{}，行数{}", keyword, pageNum, pageSize);
        List<FastOrderDto> list = new ArrayList<>();
        SearchRequest searchRequest = new SearchRequest(INDEX);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        // 设置查询条件
        QueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("content", keyword)
                .fuzziness(Fuzziness.AUTO)
                .prefixLength(3)
                .maxExpansions(10);
//                .analyzer("ik_smart");
        sourceBuilder.query(matchQueryBuilder);

        // 设置分页
        sourceBuilder.from(pageNum);
        sourceBuilder.size(pageSize);

        // 设置高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        HighlightBuilder.Field highlightTitle =
                new HighlightBuilder.Field("content");
        highlightTitle.highlighterType("unified");
        highlightBuilder.field(highlightTitle);
//        HighlightBuilder.Field highlightUser = new HighlightBuilder.Field("content");
//        highlightBuilder.field(highlightUser);
        sourceBuilder.highlighter(highlightBuilder);
        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = null;
        try {
            searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("搜索失败", e);
        }
        assert searchResponse != null;
        log.info(searchResponse.toString());
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
            // do something with the SearchHit
            String docId = hit.getId();
            String sourceAsString = hit.getSourceAsString();
            log.info(sourceAsString);
            FastOrderDto fastOrderDto = JSONObject.parseObject(sourceAsString, FastOrderDto.class);
            fastOrderDto.setId(docId);

            // 设置高亮
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            HighlightField highlight = highlightFields.get("content");
            Text[] fragments = highlight.fragments();
            String fragmentString = fragments[0].string();
            log.info(fragmentString);
            fastOrderDto.setContent(fragmentString);
            log.info(fastOrderDto.toString());
            list.add(fastOrderDto);
        }
        return list;
    }

    @Override
    public String add(Integer uid, String mobile, String content, String contactPerson, String sub) {
        IndexRequest request = new IndexRequest(INDEX);
        // 构建订单对象
        FastOrderDto fastOrderDto = new FastOrderDto();
        fastOrderDto.setContent(content);
        fastOrderDto.setUid(uid);
        fastOrderDto.setMobile(mobile);
        fastOrderDto.setContactPerson(contactPerson);
        fastOrderDto.setSub(sub);
        fastOrderDto.setPublishTime(DateUtil.date().toTimestamp());
        fastOrderDto.setIsCancel(0);
        String jsonString = JSONObject.toJSONString(fastOrderDto);
        log.info(jsonString);
        request.source(jsonString, XContentType.JSON);

        Cancellable cancellable = client.indexAsync(request, RequestOptions.DEFAULT, listener());

        return cancellable.toString();
    }

    public ActionListener<IndexResponse> listener() {
        return new ActionListener<IndexResponse>() {
            @Override
            public void onResponse(IndexResponse indexResponse) {
                log.info("listener:{}", indexResponse.toString());
                Assert.isTrue(DocWriteResponse.Result.CREATED.equals(indexResponse.getResult()));
            }

            @Override
            public void onFailure(Exception e) {
                log.error("异步请求失败");
            }
        };
    }

    @Override
    public String delete(String id) {
        // 更新文档
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("isCancel", 1);
        UpdateRequest request = new UpdateRequest(INDEX, id).doc(jsonMap);
        UpdateResponse updateResponse = null;
        try {
            updateResponse = client.update(
                    request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("删除文档失败", e);
        }
        assert updateResponse != null;
        log.info(updateResponse.toString());
        return DocWriteResponse.Result.UPDATED.equals(updateResponse.getResult()) ? "SUCCESS" : "FAILURE";
    }

    @Override
    public FastOrderDto queryById(String id) {
        try {
            GetRequest getRequest = new GetRequest(INDEX, id);
            GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);
            log.info(getResponse.toString());
            String docId = getResponse.getId();
            if (getResponse.isExists()) {
                String sourceAsString = getResponse.getSourceAsString();
                log.info(sourceAsString);
                FastOrderDto fastOrderDto = JSONObject.parseObject(sourceAsString, FastOrderDto.class);
                fastOrderDto.setId(docId);
                log.info(fastOrderDto.toString());
                return fastOrderDto;
            } else {
                log.error("该文档{}不存在", id);
            }
        } catch (ElasticsearchException | IOException exception) {
            log.error("查找文档失败", exception);
        }
        return null;
    }
}
