package cn.itcast.core.service.order;

import cn.itcast.core.pojo.order.Order;

public interface OrderService {

    /**
     * 保存订单
     * @param order
     */
    public void add(Order order,String name);
}
