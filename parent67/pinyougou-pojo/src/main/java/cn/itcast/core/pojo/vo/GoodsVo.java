package cn.itcast.core.pojo.vo;

import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.pojo.good.GoodsDesc;
import cn.itcast.core.pojo.item.Item;

import java.io.Serializable;
import java.util.List;

public class GoodsVo implements Serializable {
    private Goods goods;            //商品信息
    private GoodsDesc goodsDesc;    //商品的描述信息
    private List<Item>  items;      //库存信息itemList

    public Goods getGoods() {
        return goods;
    }

    public void setGoods(Goods goods) {
        this.goods = goods;
    }

    public GoodsDesc getGoodsDesc() {
        return goodsDesc;
    }

    public void setGoodsDesc(GoodsDesc goodsDesc) {
        this.goodsDesc = goodsDesc;
    }

    public List<Item> getItemList() {
        return items;
    }

    public void setItemList(List<Item> items) {
        this.items = items;
    }
}
