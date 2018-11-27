package cn.itcast.core.service.brand;

import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.good.Brand;

import java.util.List;
import java.util.Map;

/**
 *商品品牌
 */
public interface BrandService {
    /**
     * 查询所有品牌
     * @return
     */
    List<Brand> findAll();

    /**
     * 品牌的分页查询
     * @return
     */
    PageResult<Brand> findPage(Integer pageNum,Integer pageSize);

    /**
     * 品牌的条件分页查询
     * @param pageNum
     * @param pageSize
     * @param brand
     * @return
     */
    PageResult<Brand> search(Integer pageNum, Integer pageSize, Brand brand);

    /**
     * 添加
     * @param brand
     */
    void add(Brand brand);

    /**
     * 查询一个
     * @param id
     * @return
     */
    Brand findOne(Long id);

    /**
     * 品牌更新
     * @param brand
     */
    void update(Brand brand);

    /**
     * 批量删除
     * @param ids
     */
    void del(Long[] ids);

    /**
     * 模板中下拉选择框的使用
     * @return
     */
    List<Map<String, String>> selectOptionList();
}
