package alexmmd.top.esdemo.service;


import alexmmd.top.esdemo.domain.FastOrderDto;

import java.util.List;

/**
 * @author 汪永晖
 * @date 2021/8/25 15:42
 */
public interface EsService {

    /**
     * 全文搜索，分页
     *
     * @param keyword  关键字
     * @param pageNum  页码
     * @param pageSize 行数
     * @return 返回结果
     */
    List<FastOrderDto> search(String keyword, Integer pageNum, Integer pageSize);

    /**
     * 新增文档
     *
     * @param uid           用户唯一标识
     * @param mobile        手机号
     * @param content       订单内容
     * @param contactPerson 联系人姓名
     * @param sub           员工唯一标识
     * @return 文档的唯一标识，_id
     */
    String add(Integer uid, String mobile, String content, String contactPerson, String sub);

    /**
     * 删除文档
     *
     * @param id 文档的唯一标识
     * @return 成功或失败
     */
    String delete(String id);

    /**
     * 根据文档id查询文档
     *
     * @param id 文档唯一标识
     * @return 文档详细信息
     */
    FastOrderDto queryById(String id);
}
