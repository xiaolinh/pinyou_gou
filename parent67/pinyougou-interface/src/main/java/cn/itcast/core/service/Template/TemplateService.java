package cn.itcast.core.service.Template;

import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.template.TypeTemplate;

import java.util.List;
import java.util.Map;

public interface TemplateService {
    /**
     * 条件查询模板
     * @param page
     * @param rows
     * @param typeTemplate
     * @return
     */
    PageResult<TypeTemplate> search(Integer page, Integer rows, TypeTemplate typeTemplate);

    /**
     *新增模板
     * @param typeTemplate
     */
    void add(TypeTemplate typeTemplate);

    /**
     * 删除
     * @param ids
     */
    void delete(Long[] ids);

    /**
     * 修改
     * @param typeTemplate
     */
    void update(TypeTemplate typeTemplate);

    /**
     * 商品品牌的显示
     * @param id
     * @return
     */
    TypeTemplate findOne(Long id);

    /**
     * 根据模板id获取规格属性
     * @param id
     * @return
     */
    List<Map> findBySpecList(Long id);
}
