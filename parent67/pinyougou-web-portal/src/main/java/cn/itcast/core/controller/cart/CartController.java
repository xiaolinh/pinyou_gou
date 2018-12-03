package cn.itcast.core.controller.cart;

import cn.itcast.core.pojo.cart.Cart;
import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.order.OrderItem;
import cn.itcast.core.service.cart.CartService;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Reference
    private CartService cartService;

    @RequestMapping("/addGoodsToCartList.do")
    @CrossOrigin(origins = "http://localhost:9003")
    public Result addGoodsToCartList(Long itemId , Integer num , HttpServletRequest request, HttpServletResponse response) {
        try {
            //将商品加入购物车
            //定义一个空的购物项,以商家店铺为单位
            List<Cart> cartList = null;
            //判断传过来的cookie中有没有购物车
            Cookie[] cookies = request.getCookies();
            if (cookies != null && cookies.length > 0) {
                for (Cookie cookie : cookies) {
                    if ("BUYER_CART".equals(cookie.getName())) {
                        //浏览器有购物车
                        cartList = JSON.parseArray(cookie.getValue(), Cart.class);
                        //找到就跳出遍历,节省性能
                        break;
                    }
                }
            }
            //本地没有,那就创建一个
            if (cartList == null) {
                cartList = new ArrayList<>();
            }
            //创建购物项,封装数据
            Cart cart = new Cart();
            Item item = cartService.findOne(itemId);

            cart.setSellerId(item.getSellerId());//商家id
            List<OrderItem> orderItemList = new ArrayList<>();
            OrderItem orderItem = new OrderItem();
            orderItem.setItemId(itemId);
            orderItem.setNum(num);

            orderItemList.add(orderItem);
            cart.setOrderItemList(orderItemList);//将购物项放到购物车

            //将商品购物车
            //判断购物车中有没有该商家
            int sellerIndexOf = cartList.indexOf(cart); //如果没有返回-1
            if (sellerIndexOf != -1) {//已有商家,
                //判断是不是同一个商品
                Cart oldCart = cartList.get(sellerIndexOf);
                int itemIndexOf = oldCart.getOrderItemList().indexOf(orderItem);
                if (itemIndexOf != -1) {//有同款商品
                    OrderItem oldOrderItem = oldCart.getOrderItemList().get(itemIndexOf);
                    oldOrderItem.setNum(oldOrderItem.getNum() + num);
                } else {
                    // 无同款商品，将该购物项添加到已有的购物项集合中
                    oldCart.getOrderItemList().add(orderItem);
                }

            } else {
                // 不属于同一个商家，直接加入购物车
                cartList.add(cart);

            }

            //将购物车保存到本地cookie中
            Cookie cookie = new Cookie("BUYER_CART", JSON.toJSONString(cartList));
            cookie.setMaxAge(60*60);
            cookie.setPath("/"); // 设置cookie共享
            response.addCookie(cookie);


            return new Result(true, "加入购物车成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "加入购物车失败");

        }

    }

    @RequestMapping("/findCartList.do")
    public List<Cart> findCartList(HttpServletRequest request){
        // 未登录，从cookie取
        List<Cart> cartList = null;
        // 2、判断本地是否有购物车
        Cookie[] cookies = request.getCookies();
        if(cookies != null && cookies.length > 0){
            for (Cookie cookie : cookies) {
                if("BUYER_CART".equals(cookie.getName())){
                    // 3、本地有购物车集合
                    cartList = JSON.parseArray(cookie.getValue(), Cart.class);
                    // 找到，即可跳出循环
                    break;
                }
            }
        }
        // TODO 已登录，从redis取

        if(cartList != null){
            // 填充里面缺少的数据
            cartList = cartService.findCartList(cartList);
        }
        return cartList;
    }



}



