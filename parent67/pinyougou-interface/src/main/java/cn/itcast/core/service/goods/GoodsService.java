package cn.itcast.core.service.goods;

import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.pojo.vo.GoodsVo;

public interface GoodsService {
    /**
     * 商家添加商品
     * @param goodsVo
     */
    void add(GoodsVo goodsVo);

    /**
     * 查询商品列表信息
     * @param page
     * @param rows
     * @param goods
     * @return
     */
    PageResult<Goods> search(Integer page, Integer rows, Goods goods);

    /**
     * 修改商品的回显
     * @param id
     * @return
     */
    GoodsVo findOne(Long id);

    /**
     * 修改商品
     * @param goodsVo
     */
    void update(GoodsVo goodsVo);

    /**
     * 运营商查询未审核商品列表信息
     * @param page
     * @param rows
     * @param goods
     * @return
     */
    PageResult<Goods> searchByManager(Integer page, Integer rows, Goods goods);

    /**
     * 商品审核
     * @param ids
     * @param status
     */
    void updateStatus(Long[] ids, String status);

    /**
     * 商品的删除
     * @param ids
     */
    void delete(Long[] ids);
}
