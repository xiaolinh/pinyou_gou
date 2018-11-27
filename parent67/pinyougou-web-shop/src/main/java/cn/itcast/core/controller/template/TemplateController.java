package cn.itcast.core.controller.template;


import cn.itcast.core.pojo.template.TypeTemplate;
import cn.itcast.core.service.Template.TemplateService;

import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController//typeTemplate/findOne.do
@RequestMapping("/typeTemplate")
public class TemplateController {
    @Reference
    private TemplateService templateService;

    /**
     * 商品品牌的显示
     * @param id
     * @return
     */
    @RequestMapping("/findOne")
    public TypeTemplate findOne(Long id) {
        return templateService.findOne(id);
    }

    /**
     * 根据模板id获取规格属性
     * @param id
     * @return
     */
    @RequestMapping("/findBySpecList.do")
    public List<Map> findBySpecList(Long id) {
        return templateService.findBySpecList(id);
    }
}
