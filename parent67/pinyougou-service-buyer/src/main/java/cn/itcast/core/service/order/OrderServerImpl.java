package cn.itcast.core.service.order;

import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.dao.order.OrderDao;
import cn.itcast.core.dao.order.OrderItemDao;
import cn.itcast.core.idworker.IdWorker;
import cn.itcast.core.pojo.cart.Cart;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.order.Order;
import cn.itcast.core.pojo.order.OrderItem;

import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
public class OrderServerImpl implements OrderService{

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private OrderDao orderDao;

    @Resource
    private OrderItemDao orderItemDao;

    @Resource
    private IdWorker idWorker;

    @Resource
    private ItemDao itemDao;

    /**
     * 保存订单
     * @param order
     */
    @Transactional
    @Override
    public void add(Order order, String name) {
        // 保存订单：以商家为单位保存订单
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("BUYER_CART").get(name);
        if(cartList != null && cartList.size() > 0){
            for (Cart cart : cartList) {
                long orderId = idWorker.nextId();
                order.setOrderId(orderId);  // 订单主键
                double payment = 0f;        // 该商家下的订单总金额
                order.setPaymentType("1");  // 在线支付
                order.setStatus("1");       // 未付款
                order.setCreateTime(new Date());    // 订单创建日期
                order.setUpdateTime(new Date());    // 订单更新日期
                order.setUserId(name);  // 该订单的用户
                order.setSourceType("2");   // 订单来源：pc端
                order.setSellerId(cart.getSellerId());  // 商家id
                // 保存订单明细：取出购物项
                List<OrderItem> orderItemList = cart.getOrderItemList();
                if(orderItemList != null && orderItemList.size() > 0){
                    for (OrderItem orderItem : orderItemList) {
                        Long id = idWorker.nextId();
                        orderItem.setId(id);    // 订单明细主键
                        orderItem.setOrderId(orderId);  // 外键
                        Item item = itemDao.selectByPrimaryKey(orderItem.getItemId());
                        orderItem.setGoodsId(item.getGoodsId());    // 商品id
                        orderItem.setTitle(item.getTitle());    // 商品标题
                        orderItem.setPrice(item.getPrice());    // 商品单价
                        orderItem.setPicPath(item.getImage());  // 商品图片
                        orderItem.setSellerId(item.getSellerId());  // 商家id
                        double totalFee = item.getPrice().doubleValue() * orderItem.getNum();
                        orderItem.setTotalFee(new BigDecimal(totalFee));        // 该订单明细中的总金额
                        payment += totalFee;
                        orderItemDao.insertSelective(orderItem);
                    }
                }
                order.setPayment(new BigDecimal(payment));  // 该商家下所有订单的金额
                orderDao.insertSelective(order);
            }
        }

        // 订单提交成功清除购物车
        redisTemplate.boundHashOps("BUYER_CART").delete(name);

    }
}
