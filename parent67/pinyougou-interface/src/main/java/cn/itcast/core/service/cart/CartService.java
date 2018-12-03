package cn.itcast.core.service.cart;

import cn.itcast.core.pojo.cart.Cart;
import cn.itcast.core.pojo.item.Item;

import java.util.List;


public interface CartService {

    /**
     * 查询
     * @param itemId
     * @return
     */
    Item findOne(Long itemId);

    /**
     * 购物车数据的回显
     * @param cartList
     * @return
     */
    public List<Cart> findCartList(List<Cart> cartList);

    /**
     * 登录状态下,把本地的购物车合并到redis中
     * @param cartList
     * @param name
     */
    void mergeCartList(List<Cart> cartList, String name);

    /**
     * 用户登录后的数据车回显
     * @param name
     * @return
     */
    List<Cart> findCartListRedis(String name);
}
