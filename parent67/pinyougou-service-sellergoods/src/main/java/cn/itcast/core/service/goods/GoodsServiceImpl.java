package cn.itcast.core.service.goods;

import cn.itcast.core.dao.good.BrandDao;
import cn.itcast.core.dao.good.GoodsDao;
import cn.itcast.core.dao.good.GoodsDescDao;
import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.dao.seller.SellerDao;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.pojo.good.GoodsDesc;
import cn.itcast.core.pojo.good.GoodsQuery;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemQuery;
import cn.itcast.core.pojo.specification.Specification;
import cn.itcast.core.pojo.vo.GoodsVo;
import cn.itcast.core.service.staticpage.StaticPageService;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.opensaml.xml.signature.Q;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.SimpleFacetQuery;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;


@Service
public class GoodsServiceImpl implements GoodsService{
    @Resource
    private GoodsDao goodsDao;
    @Resource
    private GoodsDescDao goodsDescDao;

    @Resource
    private ItemDao itemDao;

    @Resource
    private ItemCatDao itemCatDao;

    @Resource
    private BrandDao brandDao;

    @Resource
    private SellerDao sellerDao;

    @Resource
    private SolrTemplate solrTemplate;

    @Resource
    private StaticPageService staticPageService;
     /**
      * 商家添加商品
      * @param goodsVo
      **/
    @Transactional
    @Override
    public void add(GoodsVo goodsVo) {
        //保存商品goods
        Goods goods = goodsVo.getGoods();
        goods.setAuditStatus("0");//设置商品审核状态
        goodsDao.insertSelective(goods);//这里返回了主键,用于保存产品明细
        //保存商品描述goodsDesc
        // 2、保存商品明细：tb_goods_desc
        // 设置外键
        GoodsDesc goodsDesc = goodsVo.getGoodsDesc();
        goodsDesc.setGoodsId(goods.getId());
        goodsDescDao.insertSelective(goodsDesc);

        //检查是否启用规格
        if ("1".equals(goods.getIsEnableSpec())) {//启用规则
            List<Item> items = goodsVo.getItemList();
             for (Item item : items) {
                // 商品标题：spu名称+spu的副标题+规格信息
                String title = goods.getGoodsName() + " " + goods.getCaption();
                // item中封装了规格：{"机身内存":"16G","网络":"联通3G"}
                String spec = item.getSpec();
                Map<String, String> map = JSON.parseObject(spec, Map.class);
                Set<Map.Entry<String, String>> entries = map.entrySet();
                for (Map.Entry<String, String> entry : entries) {
                    title += " " + entry.getValue();
                }
                item.setTitle(title);   // 商品标题
                setAttributeForItem(goods, goodsDesc, item);
                itemDao.insertSelective(item);
            }

        } else {//不使用规则
            //1对1
            Item item = new Item();
            item.setTitle(goods.getGoodsName()+" "+goods.getCaption()); // 标题
            item.setStatus("1");    // 启用的状态
            item.setIsDefault("1"); // 是否默认：本质：上架价格最低商品
            item.setSpec("{}");     // 规格
            item.setNum(9999);      // 库存量
            item.setPrice(goods.getPrice());    // 价格
            setAttributeForItem(goods, goodsDesc, item);
            itemDao.insertSelective(item);
        }
    }

    /**
     * 查询商品列表信息
     * @param page
     * @param rows
     * @param goods
     * @return
     */
    @Override
    public PageResult<Goods> search(Integer page, Integer rows, Goods goods) {
        //设置分页参数
        PageHelper.startPage(page, rows);
        // 设置查询条件
        GoodsQuery goodsQuery = new GoodsQuery();
        GoodsQuery.Criteria criteria = goodsQuery.createCriteria();
        goodsQuery.setOrderByClause("id desc"); // 根据id降序
        if(goods.getSellerId() != null && !"".equals(goods.getSellerId().trim())){
            goodsQuery.createCriteria().andSellerIdEqualTo(goods.getSellerId().trim());
        }
        // 查询
        Page<Goods> p = (Page<Goods>) goodsDao.selectByExample(goodsQuery);
        return new PageResult<Goods>(p.getTotal(), p.getResult());

    }

    /**
     * 修改商品的回显
     * @param id
     * @return
     */
    @Override
    public GoodsVo findOne(Long id) {
        GoodsVo goodsVo = new GoodsVo();
        //商品信息
        Goods goods = goodsDao.selectByPrimaryKey(id);
        goodsVo.setGoods(goods);
        //商品明细信息
        GoodsDesc goodsDesc = goodsDescDao.selectByPrimaryKey(id);
        goodsVo.setGoodsDesc(goodsDesc);
        //获取库存
        ItemQuery itemQuery = new ItemQuery();
        ItemQuery.Criteria criteria = itemQuery.createCriteria();
        criteria.andGoodsIdEqualTo(id);
        List<Item> items = itemDao.selectByExample(itemQuery);
        goodsVo.setItemList(items);
        return goodsVo;
    }

    /**
     * 修改商品
     * @param goodsVo
     */
    @Transactional
    @Override
    public void update(GoodsVo goodsVo) {
        // 更新商品
        Goods goods = goodsVo.getGoods();
        goods.setAuditStatus("0"); // 商品审核未通过，打回来重新修改因此需要重新设置审核状态
        goodsDao.updateByPrimaryKeySelective(goods);
        // 更新商品明细
        GoodsDesc goodsDesc = goodsVo.getGoodsDesc();
        goodsDescDao.updateByPrimaryKeySelective(goodsDesc);
        // 更新库存：先删后加
        ItemQuery itemQuery = new ItemQuery();
        itemQuery.createCriteria().andGoodsIdEqualTo(goods.getId());
        itemDao.deleteByExample(itemQuery);
        // 判断是否启用规格
        //检查是否启用规格
        if ("1".equals(goods.getIsEnableSpec())) {//启用规则
            List<Item> items = goodsVo.getItemList();
            for (Item item : items) {
                // 商品标题：spu名称+spu的副标题+规格信息
                String title = goods.getGoodsName() + " " + goods.getCaption();
                // item中封装了规格：{"机身内存":"16G","网络":"联通3G"}
                String spec = item.getSpec();
                Map<String, String> map = JSON.parseObject(spec, Map.class);
                Set<Map.Entry<String, String>> entries = map.entrySet();
                for (Map.Entry<String, String> entry : entries) {
                    title += " " + entry.getValue();
                }
                item.setTitle(title);   // 商品标题
                setAttributeForItem(goods, goodsDesc, item);
                itemDao.insertSelective(item);
            }

        } else {//不使用规则
            //1对1
            Item item = new Item();
            item.setTitle(goods.getGoodsName()+" "+goods.getCaption()); // 标题
            item.setStatus("1");    // 启用的状态
            item.setIsDefault("1"); // 是否默认：本质：上架价格最低商品
            item.setSpec("{}");     // 规格
            item.setNum(9999);      // 库存量
            item.setPrice(goods.getPrice());    // 价格
            setAttributeForItem(goods, goodsDesc, item);
            itemDao.insertSelective(item);
        }


    }

    /**
     * 运营商查询未审核商品列表信息
     * @param page
     * @param rows
     * @param goods
     * @return
     * 需要满足两个条件
     * 1.未审核
     * 2.未删除
     */
    @Override
    public PageResult<Goods> searchByManager(Integer page, Integer rows, Goods goods) {
        // 设置分页条件
        PageHelper.startPage(page, rows);
        // 设置查询条件
        GoodsQuery goodsQuery = new GoodsQuery();
        GoodsQuery.Criteria criteria = goodsQuery.createCriteria();
        goodsQuery.setOrderByClause("id desc"); // 根据id降序
      /*  if(goods.getAuditStatus() != null && !"".equals(goods.getAuditStatus().trim())){
            criteria.andAuditStatusEqualTo(goods.getAuditStatus().trim());//设置查询已通过的条件
        }*/
        criteria.andIsDeleteIsNull(); // 查询未删除的商品
        // 查询
        Page<Goods> p = (Page<Goods>) goodsDao.selectByExample(goodsQuery);
        return new PageResult<Goods>(p.getTotal(), p.getResult());
    }

    /**
     * 商品审核
     * @param ids
     * @param status
     */
    @Transactional
    @Override
    public void updateStatus(Long[] ids, String status) {
        //审核状态的更改
        if (ids != null && ids.length > 0) {
            Goods goods = new Goods();
            goods.setAuditStatus(status);
            for (Long id : ids) {
                goods.setId(id);
                goodsDao.updateByPrimaryKeySelective(goods);
                if ("1".equals(status)) { // 审核成功
                    // 将商品保存到索引库
                        //dataImportToSolr();//测试用
                    updateSolr(id);
                    // 生成商品详情的静态页
                    staticPageService.getHtml(String.valueOf(id));

                }
            }
        }
    }
    // 将商品保存到索引库
    private void updateSolr(Long id) {
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
     * 商品的删除
     * @param ids
     */
    @Override
    public void delete(Long[] ids) {
        if(ids != null && ids.length > 0){
            Goods goods = new Goods();
            goods.setIsDelete("1");
            for (Long id : ids) {
                goods.setId(id);
                goodsDao.updateByPrimaryKeySelective(goods);
                // TODO:将更新索引库
                SimpleQuery simpleQuery = new SimpleQuery("item_goodsid:"+id);
                solrTemplate.delete(simpleQuery);
                solrTemplate.commit();

                // TODO:删除商品详情的静态页（可选）
            }
        }

    }

    private void dataImportToSolr() {
        // 查询所有sku
        ItemQuery itemQuery = new ItemQuery();
        itemQuery.createCriteria().andStatusEqualTo("1");
        List<Item> items = itemDao.selectByExample(itemQuery);
        if(items != null && items.size() > 0){
            for (Item item : items) {
                // 处理动态字段
                String spec = item.getSpec();
                Map specMap = JSON.parseObject(spec, Map.class);
                item.setSpecMap(specMap);
            }
            solrTemplate.saveBeans(items);
            solrTemplate.commit();
        }

    }

    // 公共属性的设置
    private void setAttributeForItem(Goods goods, GoodsDesc goodsDesc, Item item) {
        // [{"color":"粉色","url":"http://192.168.25.133/group1/M00/00/00/wKgZhVmOXq2AFIs5AAgawLS1G5Y004.jpg"},
        // {"color":"黑色","url":"http://192.168.25.133/group1/M00/00/00/wKgZhVmOXrWAcIsOAAETwD7A1Is874.jpg"}]
        String itemImages = goodsDesc.getItemImages();
        List<Map> images = JSON.parseArray(itemImages, Map.class);
        if(images != null && images.size() > 0){
            item.setImage(images.get(0).get("url").toString()); // 商品图片
        }
        item.setCategoryid(goods.getCategory3Id());  // 三级分类id
        item.setCreateTime(new Date()); // 创建日期
        item.setUpdateTime(new Date()); // 更新日期
        item.setGoodsId(goods.getId()); // spu的id
        item.setSellerId(goods.getSellerId());  // 商家id
        item.setCategory(itemCatDao.selectByPrimaryKey(goods.getCategory3Id()).getName()); // 分类名称
        item.setBrand(brandDao.selectByPrimaryKey(goods.getBrandId()).getName()); // 品牌名称
        item.setSeller(sellerDao.selectByPrimaryKey(goods.getSellerId()).getNickName()); // 店铺名称
    }
}
