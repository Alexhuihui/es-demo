package alexmmd.top.esdemo.controller;

import alexmmd.top.esdemo.domain.FastOrderDto;
import alexmmd.top.esdemo.domain.OrderRequest;
import alexmmd.top.esdemo.service.EsService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author 汪永晖
 * @date 2021/8/25 16:37
 */
@RestController
@RequestMapping("/es")
public class EsController {

    @Resource
    private EsService esService;

    @PostMapping("/add")
    public String add(@RequestBody OrderRequest request) {
        return esService.add(request.getUid(), request.getMobile(), request.getContent(),
                request.getContractPerson(), "123");
    }

    @DeleteMapping("/delete")
    public String delete(@RequestParam("id") String id) {
        return esService.delete(id);
    }

    @GetMapping("/query/{id}")
    public FastOrderDto query(@PathVariable String id) {
        return esService.queryById(id);
    }

    @GetMapping("/search")
    public List<FastOrderDto> search(@RequestParam("keyword") String keyword,
                                     @RequestParam("pageNum") Integer pageNum,
                                     @RequestParam("pageSize") Integer pageSize) {
        return esService.search(keyword, pageNum, pageSize);
    }
}
