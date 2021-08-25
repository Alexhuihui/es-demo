package alexmmd.top.esdemo.domain;

import lombok.Data;

import java.sql.Timestamp;

/**
 * 极速订单
 *
 * @author HuJian
 * @date 2021/08/25 10:44:00
 */
@Data
public class FastOrderDto {
    private String id;
    /**
     * 发布用户
     */
    private Integer uid;

    /**
     * 发布用户类型
     */
    private String userType;

    /**
     * 发布时间
     */
    private Timestamp publishTime;

    /**
     * 发布状态
     */
    private Integer type;

    /**
     * 发布内容
     */
    private String content;

    /**
     * 联系人
     */
    private String contactPerson;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 员工唯一标识
     */
    private String sub;

    /**
     * 是否撤销 1撤销，0不撤销
     */
    private Integer isCancel;
}
