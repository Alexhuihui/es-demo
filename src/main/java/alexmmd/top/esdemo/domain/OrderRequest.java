package alexmmd.top.esdemo.domain;

import lombok.Data;

/**
 * @author 汪永晖
 * @date 2021/8/25 17:17
 */
@Data
public class OrderRequest {

    private String content;
    private String contractPerson;
    private String mobile;
    private Integer uid;
}
