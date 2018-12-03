package cn.itcast.core.service.cart;

import cn.itcast.core.pojo.cart.Cart;
import cn.itcast.core.pojo.item.Item;

import java.util.List;


public interface CartService {


    Item findOne(Long itemId);

    public List<Cart> findCartList(List<Cart> cartList);
}
