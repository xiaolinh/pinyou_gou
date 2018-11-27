package cn.itcast.core.service.seller;

import cn.itcast.core.dao.seller.SellerDao;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.seller.Seller;
import cn.itcast.core.pojo.seller.SellerQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;


@Service
public class SellerServiceImpl implements SellerService {
    @Resource
    private SellerDao sellerDao;

    /**
     * 商家注册
     * @param seller
     */
    @Transactional
    @Override
    public void add(Seller seller) {
        //设置商家状态,"0"未审核
        seller.setStatus("0");
        seller.setCreateTime(new Date());//设置提交日期


        if (seller.getName() != null
                && !"".equals(seller.getName().trim())
                && seller.getPassword() != null
                && !"".equals(seller.getPassword().trim())
        ) {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            String password = encoder.encode(seller.getPassword());
            seller.setPassword(password);
            sellerDao.insertSelective(seller);
        }
    }

    /**
     *
     * @param page
     * @param rows
     * @param seller
     * @return
     */
    @Override
    public PageResult<Seller> search(Integer page, Integer rows, Seller seller) {
        PageHelper.startPage(page, rows);
        SellerQuery sellerQuery = new SellerQuery();
        SellerQuery.Criteria criteria = sellerQuery.createCriteria();
        if(seller.getStatus() != null && !"".equals(seller.getStatus().trim())){//添加公司审核状态查询
            criteria.andStatusEqualTo(seller.getStatus());
        }
        if (seller.getName() != null && !"".equals(seller.getName().trim())) {//添加模糊查询公司名
            criteria.andNameLike("%" + seller.getName() + "%");
        }
        if (seller.getNickName() != null && !"".equals(seller.getNickName().trim())) {//添加模糊查询店铺名
            criteria.andNameLike("%" + seller.getNickName() + "%");
        }

        Page<Seller> pages = (Page<Seller>) sellerDao.selectByExample(sellerQuery);
        return new PageResult<Seller>(pages.getTotal(),pages.getResult());
    }

    /**
     * 查询一个商户
     * @param sellerId
     * @return
     */
    @Override
    public Seller findOne(String sellerId) {
        return sellerDao.selectByPrimaryKey(sellerId);
    }

    /**
     * 对商家进行审核
     * @param sellerId
     * @param status
     */
    @Transactional
    @Override
    public void updateStatus (String sellerId, String status) {
        Seller seller = new Seller();
        seller.setSellerId(sellerId);
        seller.setStatus(status);
        sellerDao.updateByPrimaryKeySelective(seller);
    }

}
