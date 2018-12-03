package cn.itcast.core;

import com.github.qcloudsms.SmsSingleSender;
import com.github.qcloudsms.SmsSingleSenderResult;
import com.github.qcloudsms.httpclient.HTTPException;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;


@Component
public class SmsConsumer {
    @JmsListener(destination ="itcast-sms")
    public void sendMessageToTencent(Map<String, Object> map) {

        try {
            String code = (String) map.get("code");
            String[] params = {code,"10"};
            Integer appid = (Integer) map.get("appid");
            SmsSingleSender ssender = new SmsSingleSender(appid, (String) map.get("appkey"));
            String phone = (String) map.get("phone");
            String[] phoneNumbers = {phone};
            Integer templateId = (Integer) map.get("templateId");
            String smsSign = (String) map.get("smsSign");

            SmsSingleSenderResult result = ssender.sendWithParam("86",phoneNumbers [0], templateId
                    , params, smsSign, "", "");  // 签名参数未提供或者为空时，会使用默认签名发送短信

        } catch (HTTPException e) {
            // HTTP响应码错误
            e.printStackTrace();
        } catch (JSONException e) {
            // json解析错误
            e.printStackTrace();
        } catch (IOException e) {
            // 网络IO错误
            e.printStackTrace();
        }
    }

}
