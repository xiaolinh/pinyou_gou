package cn.itcast.core.service.seller;

import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.seller.Seller;

public interface SellerService {
    void add(Seller seller);

    /**
     * 商家审核的条件分页查询
     * @param page
     * @param rows
     * @param seller
     * @return
     */
    PageResult<Seller> search(Integer page, Integer rows, Seller seller);

    /**
     * 查询一个商户
     * @param sellerId
     * @return
     */
    Seller findOne(String sellerId);

    /**
     * 对商家进行审核
     *
     * @param sellerId
     * @param status
     */
    void updateStatus(String sellerId, String status);
}
