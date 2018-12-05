package cn.itcast.core.controller.pay;

import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.service.pay.PayService;
;

import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {


    @Reference
    private PayService payService;


    /**
     * 生成订单数据
     * @return
     */
    @RequestMapping("/createNative.do")
    public Map<String,String> createNative() {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Map<String,String> map= null;
        try {
            map = payService.createNative(name);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * 查询订单的api
     * @param out_trade_no
     * @return
     */
    @RequestMapping("/queryPayStatus.do")
    public Result queryPayStatus(String out_trade_no){
        int time = 0;
        while (true){
            try {
                // 调用查询订单接口后返回的结果
                Map<String, String> map = payService.queryPayStatus(out_trade_no);
                String trade_state = map.get("trade_state");// 交易状态
                // 成功：返回flag = true
//                if("SUCCESS".equals(trade_state)){
//                    return new Result(true, "支持成功");
//                }else{
//                    // 等待支付
//                    Thread.sleep(5000);
//                    time++;
//                }
                if("NOTPAY".equals(trade_state)){
                    // 等待支付
                    Thread.sleep(5000);
                    time++;
                }else{
                    // 支付成功：可以关闭订单
                    // TODO 调用关闭订单的接口
                    return new Result(true, "支持成功");
                }
                // 30分钟的支付时间：二维码超时
                // 将程序休息5s中后，发送请求
                if(time > 360){
                    return new Result(false, "二维码超时");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
