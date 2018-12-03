package cn.itcast.core.service.cart;

import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.pojo.cart.Cart;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.order.OrderItem;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

@Service
public class CartServiceImpl  implements CartService{
    @Resource
    private ItemDao itemDao;

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    /**
     * 查询
     * @param itemId
     * @return
     */
    @Override
    public Item findOne(Long itemId) {
        return itemDao.selectByPrimaryKey(itemId);
    }

    /**
     * 购物车数据的回显
     * @param cartList
     * @return
     */
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

    /**
     * 登录状态下,把本地的购物车合并到redis中
     * @param cartList
     * @param name
     */
    @Override
    public void mergeCartList(List<Cart> cartList, String name) {
        //先从redis中取出账号中原有的
        List<Cart> oriCartList = (List<Cart>) redisTemplate.boundHashOps("BUYER_CART").get(name);

        //将本地的商品添加到oriCart中
        oriCartList = margeLocalToOriCartList(cartList, oriCartList);

        //将合并完成的购物车,重新添加到redis中
        redisTemplate.boundHashOps("BUYER_CART").put(name,oriCartList);



    }

    /**
     * 用户登录后的购物车回显
     * @param name
     * @return
     */
    @Override
    public List<Cart> findCartListRedis(String name) {
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("BUYER_CART").get(name);
        return cartList;

    }

    //将本地的商品添加到oriCart中
    private List<Cart> margeLocalToOriCartList(List<Cart> localCartList, List<Cart> oriCartList) {
        if (localCartList != null) {//本地购物车不为空
            if (oriCartList != null) {//账号中的购物车也不为空
                //需要合并
                //遍历本地购物车添加到账户中的购物车
                for (Cart cart : localCartList) {
                    //判断是否是同一个商家
                    int sellerIndexOf = oriCartList.indexOf(cart);
                    if (sellerIndexOf != -1) {//同一个商家
                        //判断是不是同一个商品
                        List<OrderItem> localOrderItemList = cart.getOrderItemList(); //本地购物项
                        List<OrderItem> OriOrderItemList = oriCartList.get(sellerIndexOf).getOrderItemList();//账户中的购物项
                        //遍历本地的购物项
                        for (OrderItem orderItem : localOrderItemList) {
                            int orderItemIndexOf = OriOrderItemList.indexOf(orderItem);
                            if (orderItemIndexOf != -1) {//有相同的商品
                                OrderItem oriOrderItem = OriOrderItemList.get(orderItemIndexOf);
                                Integer oriOrderItemNum = oriOrderItem.getNum();
                                Integer num = orderItem.getNum();
                                oriOrderItem.setNum(oriOrderItemNum + num);//合并数量
                            } else {//非同款商品
                                OriOrderItemList.add(orderItem);
                            }
                        }
                    } else {//不是同一个商家
                        oriCartList.add(cart);
                    }
                }
            } else {//账户中购物车为空
                //本地购物车覆盖账户中的购物车
                return localCartList;
            }
        } else {//本地购物车为空,不做任何操作,直接返回老车
            return oriCartList;
        }

        return oriCartList;
    }





}
