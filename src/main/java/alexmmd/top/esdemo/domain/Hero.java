package alexmmd.top.esdemo.domain;

import lombok.Data;

/**
 * @author 汪永晖
 * @date 2021/8/25 11:04
 */
@Data
public class Hero {

    private String id;

    private String name;

    private String country;

    private String birthday;

    private Long longevity;
}
