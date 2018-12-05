package cn.itcast.core.controller.order;


import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.pojo.order.Order;
import cn.itcast.core.service.order.OrderService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Reference
    private OrderService orderService;

    /**
     * 订单提交
     * @param order
     * @return
     */
    @RequestMapping("/add.do")
    public Result add(@RequestBody Order order){

        try {
            String name = SecurityContextHolder.getContext().getAuthentication().getName();
            orderService.add(order, name);
            return new Result(true, "提交成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "提交失败");
        }
    }
}
