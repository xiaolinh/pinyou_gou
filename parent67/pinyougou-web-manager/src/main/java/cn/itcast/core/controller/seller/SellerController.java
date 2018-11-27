package cn.itcast.core.controller.seller;

import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.pojo.seller.Seller;
import cn.itcast.core.service.seller.SellerService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seller")
public class SellerController {
    @Reference
    private SellerService sellerService;

    /**
     * 商家审核的条件分页查询
     * @param page
     * @param rows
     * @param seller
     * @return
     */
    @RequestMapping("/search.do")
    public PageResult<Seller> search(Integer page,Integer rows ,@RequestBody Seller seller) {
        return sellerService.search(page,rows,seller);
    }

    /**
     * 查询一个商户
     * @param id
     * @return
     */
    @RequestMapping("/findOne.do")
    public Seller findOne(String id) {
        return sellerService.findOne(id);
    }

    @RequestMapping("/updateStatus.do")
    public Result updateStatus(String sellerId, String status) {
        try {
            sellerService.updateStatus(sellerId,status);
            return new Result(true, "审核完成");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(true, "审核未完成");
        }
    }
}
