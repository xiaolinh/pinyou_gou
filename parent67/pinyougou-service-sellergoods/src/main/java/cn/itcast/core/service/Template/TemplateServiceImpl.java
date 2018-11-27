package cn.itcast.core.service.Template;

import cn.itcast.core.dao.specification.SpecificationOptionDao;
import cn.itcast.core.dao.template.TypeTemplateDao;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.specification.SpecificationOption;
import cn.itcast.core.pojo.specification.SpecificationOptionQuery;
import cn.itcast.core.pojo.template.TypeTemplate;
import cn.itcast.core.pojo.template.TypeTemplateQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.opensaml.xml.signature.J;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.BinaryClient;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;


@Service
public class TemplateServiceImpl implements TemplateService {

    @Resource
    private TypeTemplateDao typeTemplateDao;//模板
    @Resource
    private SpecificationOptionDao specificationOptionDao;//规格表

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    /**
     * 模板条件分页查询模板
     * @param page 当前页
     * @param rows 每页显示的个数
     * @param typeTemplate  条件
     * @return
     */
    @Override
    public PageResult<TypeTemplate> search(Integer page, Integer rows, TypeTemplate typeTemplate) {
        //将品牌以及规格放入缓存中
        List<TypeTemplate> typeTemplates = typeTemplateDao.selectByExample(null);
        if (typeTemplates != null && typeTemplates.size() > 0) {
            for (TypeTemplate template : typeTemplates) {
                String brandIds = template.getBrandIds();
                List<Map> lists = JSON.parseArray(brandIds, Map.class);
                redisTemplate.boundHashOps("brandList").put(template.getId(),lists);
                //缓存规格选项
                List<Map> specList = findBySpecList(template.getId());
                redisTemplate.boundHashOps("specList").put(template.getId(),specList);
            }
        }


        //设置分页助手
        PageHelper.startPage(page, rows);
        //设置查询条件
        TypeTemplateQuery templateQuery = new TypeTemplateQuery();
        TypeTemplateQuery.Criteria criteria = templateQuery.createCriteria();
        if (typeTemplate.getName() != null && !"".equals(typeTemplate.getName().trim())) {
            criteria.andNameLike("%"+typeTemplate.getName()+"%");
        }
        //设置降序查询
        templateQuery.setOrderByClause("id desc");
        //查询并返回数据
        Page<TypeTemplate> pages = (Page<TypeTemplate>) typeTemplateDao.selectByExample(templateQuery);

        return new PageResult<TypeTemplate>(pages.getTotal(),pages.getResult());
    }

    /**
     * 新增模板
     * @param typeTemplate
     */
    @Transactional
    @Override
    public void add(TypeTemplate typeTemplate) {
        typeTemplateDao.insertSelective(typeTemplate);
    }

    /**
     * 批量删除模板
     * @param ids
     */
    @Transactional
    @Override
    public void delete(Long[] ids) {
        typeTemplateDao.deleteByPrimaryKeys(ids);
    }

    /**
     * 查询一个
     * @param id
     * @return
     */
    @Override
    public TypeTemplate findOne(Long id) {
        return typeTemplateDao.selectByPrimaryKey(id);
    }

    @Transactional
    @Override
    public void update(TypeTemplate typeTemplate) {

        typeTemplateDao.updateByPrimaryKey(typeTemplate);
    }

    /**
     * 根据模板id获取规格属性
     * @param id
     * @return
     */
    @Override
    public List<Map> findBySpecList(Long id) {
        TypeTemplate typeTemplate = typeTemplateDao.selectByPrimaryKey(id);
        // 例如：[{"id":27,"text":"网络"},{"id":32,"text":"机身内存"}]
        String specIds = typeTemplate.getSpecIds();
       //把specIds转换成list<Map>集合
        List<Map> maps = JSON.parseArray(specIds, Map.class);
        for (Map map : maps) {
            //获取单个规格id
            Long specId = Long.parseLong(map.get("id").toString());
            // 根据规格id获取对应规格选项（属性）,查询规格表
            SpecificationOptionQuery specificationOptionQuery = new SpecificationOptionQuery();
            specificationOptionQuery.createCriteria().andSpecIdEqualTo(specId);
            List<SpecificationOption> options = specificationOptionDao.selectByExample(specificationOptionQuery);
            map.put("options", options);

        }

        // list:[{"id":27,"text":"网络","options":[{},{}...]}]
        return maps;
    }
}
