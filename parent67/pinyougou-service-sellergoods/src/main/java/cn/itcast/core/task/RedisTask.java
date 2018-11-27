package cn.itcast.core.task;

import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.dao.specification.SpecificationOptionDao;
import cn.itcast.core.dao.template.TypeTemplateDao;
import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.pojo.specification.SpecificationOption;
import cn.itcast.core.pojo.specification.SpecificationOptionQuery;
import cn.itcast.core.pojo.template.TypeTemplate;
import com.alibaba.fastjson.JSON;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Component
public class RedisTask {
    @Resource
    private TypeTemplateDao typeTemplateDao;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private SpecificationOptionDao specificationOptionDao;

    @Resource
    private ItemCatDao itemCatDao;


    @Scheduled(cron = "0 0 16 * * ?")
    public void setBrandAndSpecsToRedis() {
        //将模板数据缓存到redis中
        List<TypeTemplate> templateList = typeTemplateDao.selectByExample(null);
        if (templateList != null && templateList.size()>0) {
            for (TypeTemplate typeTemplate : templateList) {
                //缓存改模板的品牌
                String brandIds = typeTemplate.getBrandIds();
                List<Map> brandList= JSON.parseArray(brandIds, Map.class);
                redisTemplate.boundHashOps("brandList").put(typeTemplate.getId(),brandList);
                // 缓存该模板的规格（选项）
                List<Map> specList = findBySpecList(typeTemplate.getId());
                redisTemplate.boundHashOps("specList").put(typeTemplate.getId(), specList);
            }
        }
        System.out.println("定时器执行啦，模板到Redis啦啦啦。。。");
    }

    @Scheduled(cron = "0 0 16 * * ?")
    public void setItemCatToRedis(){
        // 将所有的商品分类缓存到redis中
        List<ItemCat> itemCatList = itemCatDao.selectByExample(null);
        if(itemCatList != null && itemCatList.size() > 0){
            for (ItemCat itemCat : itemCatList) {
                // 栗子：手机--模板id
                redisTemplate.boundHashOps("itemCat").put(itemCat.getName(), itemCat.getTypeId());
            }
        }
        System.out.println("定时器执行啦，分类到Redis啦啦啦。。。");

    }

    public List<Map> findBySpecList(Long id) {
        TypeTemplate typeTemplate = typeTemplateDao.selectByPrimaryKey(id);
        // spec_ids:[{"id":27,"text":"网络"},{"id":32,"text":"机身内存"}]
        String specIds = typeTemplate.getSpecIds();
        // 将该json串转成集合(具体规格)
        List<Map> list = JSON.parseArray(specIds, Map.class);
        // 获取该规格对应的规格选项信息
        for (Map map : list) {
//			Long specId = (Long) map.get("id");
            Long specId = Long.parseLong(map.get("id").toString());
            // 获取规格选项
            SpecificationOptionQuery example = new SpecificationOptionQuery();
            example.createCriteria().andSpecIdEqualTo(specId);
            List<SpecificationOption> options = specificationOptionDao.selectByExample(example);
            // 将该数据放入map中
            map.put("options", options);
        }
        return list;
    }
}





