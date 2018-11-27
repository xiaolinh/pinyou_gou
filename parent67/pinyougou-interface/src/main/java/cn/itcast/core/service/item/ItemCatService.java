package cn.itcast.core.service.item;

import cn.itcast.core.pojo.item.ItemCat;

import java.util.List;
import java.util.Map;

public interface ItemCatService {
    /**
     * 商品分类列表查询
     * @param parentId
     * @return
     */
    List<ItemCat> findByParentId(Long parentId);

    ItemCat findOne(Long id);

    List<ItemCat> findAll();


}
