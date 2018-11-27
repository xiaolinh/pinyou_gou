package cn.itcast.core.service.Specification;

import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.specification.Specification;
import cn.itcast.core.pojo.vo.SpecificationVo;

import java.util.List;
import java.util.Map;

public interface SpecificationService   {


    /**
     * 搜索查询
     * @param page
     * @param rows
     * @param specification
     * @return
     */
    PageResult<Specification> search(Integer page, Integer rows, Specification specification);

    /**
     * 添加商品规格
     * @param specificationVo
     */
    void add(SpecificationVo specificationVo);

    /**
     * 查询一个实体
     * @param id
     * @return
     */
    SpecificationVo findOne(Long id);

    /**
     * 修改规则
     * @param specificationVo
     */
    void update(SpecificationVo specificationVo);

    /**
     * 删除规格模板
     * @param ids
     */
    void delete(Long[] ids);

    List<Map<String, String>> selectOptionList();
}
