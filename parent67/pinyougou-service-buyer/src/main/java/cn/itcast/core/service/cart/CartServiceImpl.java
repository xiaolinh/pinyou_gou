package cn.itcast.core.service.cart;

import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.pojo.cart.Cart;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.order.OrderItem;
import com.alibaba.dubbo.config.annotation.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

@Service
public class CartServiceImpl  implements CartService{
    @Resource
    private ItemDao itemDao;


    @Override
    public Item findOne(Long itemId) {
        return itemDao.selectByPrimaryKey(itemId);
    }

    @Override
    public List<Cart> findCartList(List<Cart> cartList) {
        // 将页面需要展示的数据填充进来
        for (Cart cart : cartList) {
            List<OrderItem> orderItemList = cart.getOrderItemList();
            for (OrderItem orderItem : orderItemList) {
                Item item = itemDao.selectByPrimaryKey(orderItem.getItemId());
                cart.setSellerName(item.getSeller()); // 商家店铺名称
                orderItem.setPicPath(item.getImage()); // 商品图片
                orderItem.setPrice(item.getPrice()); // 商品单价
                orderItem.setTitle(item.getTitle()); // 商品标题
                BigDecimal totalFee = new BigDecimal(item.getPrice().doubleValue() * orderItem.getNum());
                orderItem.setTotalFee(totalFee); // 商品总价
            }
        }
        return cartList;

    }
}
