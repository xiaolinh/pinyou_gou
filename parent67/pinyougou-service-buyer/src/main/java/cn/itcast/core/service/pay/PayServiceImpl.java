package cn.itcast.core.service.pay;



import cn.itcast.core.dao.log.PayLogDao;
import cn.itcast.core.idworker.IdWorker;
import cn.itcast.core.pojo.log.PayLog;

import cn.itcast.core.wx.HttpClient;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
@Service
public class PayServiceImpl implements PayService {

    @Resource
    private IdWorker idWorker;

    @Value("${appid}")
    private String appid;       // 公众号id

    @Value("${partner}")
    private String partner;     // 商户账号

    @Value("${partnerkey}")
    private String partnerkey;  // 商户秘钥

    @Value("${notifyurl}")
    private String notifyurl;   // 回调地址

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private PayLogDao payLogDao;

    /**
     * 支付页面需要的数据
     * @return
     */
    @Override
    public Map<String, String> createNative(String username) throws Exception{

        // 从缓存中取出交易日志的数据
        PayLog payLog = (PayLog) redisTemplate.boundHashOps("paylog").get(username);

        Map<String, String> data = new HashMap<>();
        long out_trade_no = idWorker.nextId();
        // 调用微信统一下单的接口
        String url = "https://api.mch.weixin.qq.com/pay/unifiedorder";
//        公众账号ID 	appid 	是 	String(32) 	wxd678efh567hg6787 	微信支付分配的公众账号ID（企业号corpid即为此appId）
        data.put("appid", appid);
//        商户号 	mch_id 	是 	String(32) 	1230000109 	微信支付分配的商户号
        data.put("mch_id", partner);
//        随机字符串 	nonce_str 	是 	String(32) 	5K8264ILTKCH16CQ2502SI8ZNMTM67VS 	随机字符串，长度要求在32位以内。推荐随机数生成算法
        data.put("nonce_str", WXPayUtil.generateNonceStr());
//        签名 	sign 	是 	String(32) 	C380BEC2BFD727A4B6845133519F3AD6 	通过签名算法计算得出的签名值，详见签名生成算法

//        商品描述 	body 	是 	String(128) 	腾讯充值中心-QQ会员充值
        data.put("body", "品优购订单支付");
//        商户订单号 	out_trade_no 	是 	String(32) 	20150806125346 	商户系统内部订单号，要求32个字符内，只能是数字、大小写字母_-|* 且在同一个商户号下唯一。详见商户订单号
        data.put("out_trade_no", String.valueOf(out_trade_no));
        //data.put("out_trade_no", payLog.getOutTradeNo());
//        标价金额 	total_fee 	是 	Int 	88 	订单总金额，单位为分，详见支付金额
//        data.put("total_fee", String.valueOf(payLog.getTotalFee())); // 实际支付的金额
        data.put("total_fee", "1");
//        终端IP 	spbill_create_ip 	是 	String(16) 	123.12.12.123 	APP和网页支付提交用户端ip，Native支付填调用微信支付API的机器IP。
        data.put("spbill_create_ip", "123.12.12.123");
//        通知地址 	notify_url 	是 	String(256) 	http://www.weixin.qq.com/wxpay/pay.php 	异步接收微信支付结果通知的回调地址，通知url必须为外网可访问的url，不能携带参数。
        data.put("notify_url", notifyurl);
//        交易类型 	trade_type 	是 	String(16) 	JSAPI
        data.put("trade_type", "NATIVE");
        // 入参以及返回值都是xml
        String xmlParam = WXPayUtil.generateSignedXml(data, partnerkey);
        System.out.println("发送的数据：");
        System.out.println(xmlParam);
        // 发送请求：httpclient发送
        HttpClient httpClient = new HttpClient(url);
//        httpClient.isHttps();   // https请求
        httpClient.setHttps(true);
        httpClient.setXmlParam(xmlParam);   // 请求的数据
        httpClient.post();      // post提交

        // 有响应：有返回值
        String strXML = httpClient.getContent(); // xml
        System.out.println("统一下单接口的返回值：");
        System.out.println(strXML);
        // 将xml装map
        Map<String, String> map = WXPayUtil.xmlToMap(strXML);

        // 订单号
        data.put("out_trade_no", String.valueOf(out_trade_no));
        // 支付金额
        data.put("total_fee", "1"); // 显示需要支付的金额
        // 二维码的url---微信支付url
        data.put("code_url", map.get("code_url"));
        return data;
    }

    /**
     * 查询订单
     * @param out_trade_no
     * @return
     */
    @Override
    public Map<String, String> queryPayStatus(String out_trade_no) throws Exception{
        Map<String, String> data = new HashMap<>();
        // 微信查询订单的接口
        String url = "https://api.mch.weixin.qq.com/pay/orderquery";
        // 请求参数
//        公众账号ID	appid	是	String(32)	wxd678efh567hg6787	微信支付分配的公众账号ID（企业号corpid即为此appId）
        data.put("appid", appid);
//        商户号	mch_id	是	String(32)	1230000109	微信支付分配的商户号
        data.put("mch_id", partner);
//        商户订单号	out_trade_no	String(32)	20150806125346	商户系统内部订单号，要求32个字符内，只能是数字、大小写字母_-|*@ ，且在同一个商户号下唯一。 详见商户订单号
        data.put("out_trade_no", out_trade_no);
//        随机字符串	nonce_str	是	String(32)	C380BEC2BFD727A4B6845133519F3AD6	随机字符串，不长于32位。推荐随机数生成算法
        data.put("nonce_str", WXPayUtil.generateNonceStr());
//        签名	sign	是	String(32)	5K8264ILTKCH16CQ2502SI8ZNMTM67VS	通过签名算法计算得出的签名值，详见签名生成算法
        String xmlParam = WXPayUtil.generateSignedXml(data, partnerkey);

        // 发送请求
        HttpClient httpClient = new HttpClient(url);
        httpClient.setHttps(true);
        httpClient.setXmlParam(xmlParam);
        httpClient.post();

        // 响应结果
        String strXML = httpClient.getContent();
        System.out.println("查询订单接口返回值");
        System.out.println(strXML);
        Map<String, String> map = WXPayUtil.xmlToMap(strXML);
        // 如果成功，需要更新支付日志
        if("SUCCESS".equals(map.get("trade_state"))){
            // 更新支付日志
            PayLog payLog = new PayLog();
            payLog.setOutTradeNo(out_trade_no); // 主键
            payLog.setPayTime(new Date());  // 支付时间
            payLog.setTransactionId(map.get("transaction_id")); // 第三方交易流水
            payLog.setTradeState("1");  // 交易成功
            payLogDao.updateByPrimaryKeySelective(payLog);
            // TODO 删除缓存中的交易日志
        }
        return map;
    }
}
