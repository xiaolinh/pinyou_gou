package cn.itcast.core.service.staticpage;

import cn.itcast.core.dao.good.GoodsDao;
import cn.itcast.core.dao.good.GoodsDescDao;
import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.pojo.good.GoodsDesc;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.pojo.item.ItemQuery;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StaticPageServiceImpl implements StaticPageService , ServletContextAware {

    @Resource
    private GoodsDao goodsDao;

    @Resource
    private GoodsDescDao goodsDescDao;

    @Resource
    private ItemDao itemDao;

    @Resource
    private ItemCatDao itemCatDao;


    private Configuration configuration;
    //注入freemarkerConfigurer好处:指定模板时的路径,以及编码格式.
    public void setFreeMarkerConfigurer(FreeMarkerConfigurer freeMarkerConfigurer) {
        this.configuration = freeMarkerConfigurer.getConfiguration();
    }

    private ServletContext servletContext;
   @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }




    @Override
    public void getHtml(String id) {
        //创建configuration并制定模板位置
            //可以通过注入解决
        //通过configuration获取制定模板
        try {
            Template template = configuration.getTemplate("item.ftl");

            //准备业务数据
            Map<String, Object> dataModel = getDataModel(id);

            //模板+数据 = 输出

            String pathname = "/" + id + ".html";
            String path = servletContext.getRealPath(pathname);
            File file = new File(path);
            Writer out = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");

            template.process(dataModel,out);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private Map<String, Object> getDataModel(String id) {

        Map<String, Object> dataModel = new HashMap<>();
        //商品数据
        Goods goods = goodsDao.selectByPrimaryKey(Long.parseLong(id));
        dataModel.put("goods", goods);
        //商品明细
        GoodsDesc goodsDesc = goodsDescDao.selectByPrimaryKey(Long.parseLong(id));
        dataModel.put("goodsDesc", goodsDesc);
        //商品分类
        ItemCat itemCat1 = itemCatDao.selectByPrimaryKey(goods.getCategory1Id());
        ItemCat itemCat2 = itemCatDao.selectByPrimaryKey(goods.getCategory2Id());
        ItemCat itemCat3 = itemCatDao.selectByPrimaryKey(goods.getCategory3Id());
        dataModel.put("itemCat1", itemCat1);
        dataModel.put("itemCat2", itemCat2);
        dataModel.put("itemCat3", itemCat3);

        //库存数据
        ItemQuery itemQuery = new ItemQuery();
        itemQuery.createCriteria().andGoodsIdEqualTo(Long.parseLong(id)).andNumGreaterThan(0);
        List<Item> itemList = itemDao.selectByExample(itemQuery);
        dataModel.put("itemList", itemList);

        return dataModel;
    }
}
