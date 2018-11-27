package cn.itcast.core.controller.item;

import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.service.item.ItemCatService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/itemCat")
public class ItemCatController {
    @Reference
    private ItemCatService itemCatService;

    /**
     * 商品分类列表查询
     * @param parentId
     * @return
     */
    @RequestMapping("/findByParentId.do")
    public List<ItemCat> findByParentId(Long parentId) {
        return itemCatService.findByParentId(parentId);
    }

    @RequestMapping("/findOne.do")
    public ItemCat findOne(Long id) {
        return itemCatService.findOne(id);
    }

    /**
     * 查询所有分类列表
     * @return
     */
    @RequestMapping("/findAll.do")
    public List<ItemCat> findAll(){
        return itemCatService.findAll();
    }

}
