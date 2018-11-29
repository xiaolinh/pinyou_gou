package cn.itcast.core.service.search;

import java.util.Map;

public interface ItemSearchService {
    /**
     * 前台系统检索
     * @param searchMap
     * @return
     */
    Map<String, Object> search(Map<String, String> searchMap);

    /**
     * 审核通过把商品添加到索引库
     * @param id
     */
    public void updateSolr(Long id);

    /**
     * 删除索引
     * @param id
     */
    public void deleteItemFromSolr(Long id);
}
