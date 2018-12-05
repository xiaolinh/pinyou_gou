package cn.itcast.core.service.pay;

import java.util.Map;

public interface PayService  {

    /**
     * 生成订单数据
     * @return
     */
    Map<String, String> createNative(String username)throws Exception;

    /**
     * 查询订单
     * @param out_trade_no
     * @return
     */
    Map<String, String> queryPayStatus(String out_trade_no) throws Exception;
}
