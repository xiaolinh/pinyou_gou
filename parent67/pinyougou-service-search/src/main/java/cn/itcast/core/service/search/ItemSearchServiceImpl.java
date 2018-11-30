package cn.itcast.core.service.search;


import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;


import javax.annotation.Resource;
import java.util.*;

@Service
public class ItemSearchServiceImpl implements ItemSearchService {
    @Resource
    private SolrTemplate solrTemplate;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private ItemDao itemDao;

    /**
     *  前台系统检索
     * @param searchMap
     * @return
     */
    @Override
    public Map<String, Object> search(Map<String, String> searchMap) {
        Map<String, Object> resultMap = new HashMap<>();
        //处理关键字
        String keywords = searchMap.get("keywords");
        if (keywords != null && !"".equals(keywords)) {
            keywords.replace(" ", "");//替换掉空格
            searchMap.put("keywords", keywords);
        }


        // 商品结果集
       // Map<String, Object> map = searchPage(searchMap);
        Map<String, Object> map = searchForHighLightPage(searchMap);
        resultMap.putAll(map);

        //商品列表分类分组查询
        List<String> categoryList = searchForGroupPage(searchMap);
        if(categoryList != null && categoryList.size() > 0){
            resultMap.put("categoryList", categoryList);
            //默认加载第一个分类下的品牌以及规则
            Map<String, Object> brandAndSpecMap = searchBrandAndSpecListByCategory(categoryList.get(0));

        }


        return resultMap;
    }
    //默认加载第一个分类下的品牌以及规则
    private Map<String, Object> searchBrandAndSpecListByCategory(String category) {
        Map<String, Object> brandAndSpecMap = new HashMap<>();
        // 获取模板id
        Object typeId = redisTemplate.boundHashOps("itemCat").get(category);
        // 获取品牌
        List<Map> brandList = (List<Map>) redisTemplate.boundHashOps("brandList").get(typeId);
        // 获取规格
        List<Map> specList = (List<Map>) redisTemplate.boundHashOps("specList").get(typeId);
        brandAndSpecMap.put("brandList", brandList);
        brandAndSpecMap.put("specList", specList);
        return brandAndSpecMap;

    }

    //商品列表分类分组查询
    private List<String> searchForGroupPage(Map<String, String> searchMap) {
        //设置关键字,封装关键字
        Criteria criteria = new Criteria("item_keywords");
        criteria.is(searchMap.get("keywords"));
        SimpleQuery simpleQuery = new SimpleFacetQuery(criteria);
        //设置分组并查询
        GroupOptions groupOptions = new GroupOptions();
        groupOptions.addGroupByField("item_category");  //根据那个字段进行分组
        simpleQuery.setGroupOptions(groupOptions);
        GroupPage<Item> groupPage = solrTemplate.queryForGroupPage(simpleQuery, Item.class);
        //获取分组结果
        List<String> list = new ArrayList<>();
        GroupResult<Item> groupResult = groupPage.getGroupResult("item_category");
        Page<GroupEntry<Item>> groupEntries = groupResult.getGroupEntries();    //获取分组结果数组
        for (GroupEntry<Item> groupEntry : groupEntries) {
            String groupValue = groupEntry.getGroupValue();
            if (groupValue!=null) {
                list.add(groupValue);
            }
        }

        return list;
    }

    //前台系统关键字高亮显示
    private Map<String, Object> searchForHighLightPage(Map<String, String> searchMap) {
        //获取关键字
        String keywords = searchMap.get("keywords");
        //封装检索条件
        Criteria criteria = new Criteria("item_keywords");
        //判断下是否有条件
        if (keywords != null && !"".equals(keywords)) {//有条件就把这个条件加入到检索条件中
            criteria.is(keywords);//模糊查询条件
        }
        //获取高亮分页查询
        SimpleHighlightQuery query = new SimpleHighlightQuery(criteria);
        //添加分页条件
        Integer pageNo = Integer.valueOf(searchMap.get("pageNo"));      //当前页码
        Integer pageSize = Integer.valueOf(searchMap.get("pageSize"));  //总条数
        Integer offset = (pageNo - 1) * pageSize;
        query.setOffset(offset);
        query.setRows(pageSize);

        //设置高亮字段
        HighlightOptions options = new HighlightOptions();
        options.setSimplePrefix("<font color='red'>");  //设置开始
        options.addField("item_title");                 //设置高亮的字段
        options.setSimplePostfix("</font>");            //设置结尾
        query.setHighlightOptions(options);

     //根据条件过滤
        //根据分类过滤
        if (searchMap.get("category") != null && !"".equals(searchMap.get("category"))) {
            Criteria cri= new Criteria("item_category");
            cri.is(searchMap.get("category"));
            FilterQuery filterQuery = new SimpleFacetQuery(cri);
            query.addFilterQuery(filterQuery);
        }
        // 根据品牌过滤
        if(searchMap.get("brand") != null && !"".equals(searchMap.get("brand"))){
            Criteria cri = new Criteria("item_brand");
            cri.is(searchMap.get("brand"));
            FilterQuery filterQuery = new SimpleFilterQuery(cri);
            query.addFilterQuery(filterQuery);
        }
        // 根据价格过滤
        if(searchMap.get("price") != null && !"".equals(searchMap.get("price"))){
            String[] prices = searchMap.get("price").split("-");
            Criteria cri = new Criteria("item_price");
            if(searchMap.get("price").contains("*")){ // xxx以上
                cri.greaterThan(prices[0]);
            }else{ // 区间段
                cri.between(prices[0], prices[1], true, true);
            }
            FilterQuery filterQuery = new SimpleFilterQuery(cri);
            query.addFilterQuery(filterQuery);
        }
        // 根据商品规格过滤
        if(searchMap.get("spec") != null && !"".equals(searchMap.get("spec"))){
            Map<String, String> specMap = JSON.parseObject(searchMap.get("spec"), Map.class);
            Set<Map.Entry<String, String>> entrySet = specMap.entrySet();
            for (Map.Entry<String, String> entry : entrySet) {
                Criteria cri = new Criteria("item_spec_"+entry.getKey());
                cri.is(entry.getValue());
                FilterQuery filterQuery = new SimpleFilterQuery(cri);
                query.addFilterQuery(filterQuery);
            }
        }

    // 根据价格以及新品排序：sortField：排序字段    sort：传递的值
        if (searchMap.get("sort") != null && !"".equals(searchMap.get("sort"))) {
            if ("ASC".equals(searchMap.get("sort"))) {
                Sort sort = new Sort(Sort.Direction.ASC, "item_" + searchMap.get("sortField"));
                query.addSort(sort);
            } else {
                Sort sort = new Sort(Sort.Direction.DESC, "item_" + searchMap.get("sortField"));
                query.addSort(sort);
            }
        }



        // 根据条件查询
        HighlightPage<Item> items = solrTemplate.queryForHighlightPage(query, Item.class);
        List<HighlightEntry<Item>> highlighted = items.getHighlighted();
        if(highlighted != null && highlighted.size() > 0){
             for (HighlightEntry<Item> highlightEntry : highlighted) {
                 Item item = highlightEntry.getEntity(); // 普通结果
                 List<HighlightEntry.Highlight> highlights = highlightEntry.getHighlights();//获取高亮结果
                 if(highlights != null && highlights.size() > 0){
                     String s =highlights.get(0).getSnipplets().get(0);//获取到高亮的字段
                     item.setTitle(s);
                 }
            }
        }


        // 处理结果集
        Map<String, Object> map = new HashMap<>();
        map.put("totalPages", items.getTotalPages());//总页数
        map.put("total", items.getTotalElements());  //总条数
        map.put("rows", items.getContent());         //结果集
        return map;
    }


    // 关键字检索并且分页
    private Map<String, Object> searchPage(Map<String, String> searchMap) {//searchMap中:keywords:关键字字段, pageNo:当前页码,pageSize:总条数
        //根据关键字查找
        String keywords = searchMap.get("keywords");
        //设置关键字
        Criteria criteria = new Criteria("item_keywords");
        //判断下是否有条件
        if (keywords != null && !"".equals(keywords)) {//有条件就把这个条件加入到检索条件中
            criteria.is(keywords);//模糊查询条件
        }
        //设置查询条件
        SimpleQuery query = new SimpleQuery(criteria);
        //添加分页文件
        Integer pageNo = Integer.valueOf(searchMap.get("pageNo"));      //当前页码
        Integer pageSize = Integer.valueOf(searchMap.get("pageSize"));  //总条数
        Integer offset = (pageNo - 1) * pageSize;
        query.setOffset(offset);        //起始个数
        query.setRows(pageSize);        //总的个数

        // 根据条件查询
        ScoredPage<Item> scoredPage = solrTemplate.queryForPage(query, Item.class);
        // 处理结果集
        Map<String, Object> map = new HashMap<>();
        map.put("totalPages", scoredPage.getTotalPages());//总页数
        map.put("total", scoredPage.getTotalElements());  //总条数
        map.put("rows", scoredPage.getContent());         //结果集
        return map;
    }

    @Override
    public void updateSolr(Long id) {
        // 根据商品id查询对应的库存信息
        ItemQuery itemQuery = new ItemQuery();
        itemQuery.createCriteria().andStatusEqualTo("1").
                andIsDefaultEqualTo("1").andGoodsIdEqualTo(id);
        List<Item> list = itemDao.selectByExample(itemQuery);
        if(list != null && list.size() > 0){
            for (Item item : list) {
                // 处理动态字段
                String spec = item.getSpec();
                Map specMap = JSON.parseObject(spec, Map.class);
                item.setSpecMap(specMap);
            }
            solrTemplate.saveBeans(list);
            solrTemplate.commit();
        }

    }

    /**
     * 删除商品索引
     * @param id
     */
    @Override
    public void deleteItemFromSolr(Long id) {
        SimpleQuery query = new SimpleQuery("item_goodsid:"+id);
        solrTemplate.delete(query);
        solrTemplate.commit();

    }
}
