package cn.itcast.core.service.brand;

import cn.itcast.core.dao.good.BrandDao;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.pojo.good.BrandQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
public class BrandServiceImpl implements  BrandService {
    //这里使用jdk中提供的,而不是用spring中@aoutwire 为了节省资源,提高性能
    @Resource
    private BrandDao brandDao;

    @Override
    public List<Brand> findAll() {
        return brandDao.selectByExample(null);
    }

    /**
     * 品牌的分页查询
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public PageResult<Brand> findPage(Integer pageNum, Integer pageSize) {
        //设置分页助手的参数
        PageHelper.startPage(pageNum,pageSize);
        //根据条件查询,没有条件就是null;
        Page<Brand> page = (Page<Brand>) brandDao.selectByExample(null);
        //将结果封装到分页对象
        return new PageResult<Brand>(page.getTotal(), page.getResult());
    }

    /**
     * 品牌的条件分页查询
     * @param pageNum
     * @param pageSize
     * @param brand
     * @return
     */
    @Override
    public PageResult<Brand> search(Integer pageNum, Integer pageSize, Brand brand) {
        //设置分页助手的参数
        PageHelper.startPage(pageNum,pageSize);
        //设置查询条件,在sql语句中拼接条件
        BrandQuery brandQuery = new BrandQuery();
        BrandQuery.Criteria criteria = brandQuery.createCriteria();
        if(brand.getName() != null && !"".equals(brand.getName().trim())){
            // 条件存在
            criteria.andNameLike("%" + brand.getName().trim() + "%");
        }
        if(brand.getFirstChar() != null && !"".equals(brand.getFirstChar().trim())){
            criteria.andFirstCharEqualTo(brand.getFirstChar().trim());
        }
        //根据id降序
        brandQuery.setOrderByClause("id desc");
        //开始查询
        Page<Brand> page = (Page<Brand>) brandDao.selectByExample(brandQuery);

        //将结果封装到分页对象
        return new PageResult<Brand>(page.getTotal(), page.getResult());
    }

    /**
     * 保存品牌
     * @param brand
     */
    @Transactional
    @Override
    public void add(Brand brand) {
         brandDao.insertSelective(brand);
    }

    @Override
    public Brand findOne(Long id) {
        return brandDao.selectByPrimaryKey(id);
    }

    @Override
    public void update(Brand brand) {
        brandDao.updateByPrimaryKey(brand);
    }

    /**
     * 批量删除
     * @param ids
     */
    @Override
    public void del(Long[] ids) {
        if(ids != null && ids.length > 0){
           /*for (Long id : ids) {
                brandDao.deleteByPrimaryKey(id);
            }*/
           //自定义的批量删除方法,更加节省性能,不会因为数据太多而导致内存溢出
            brandDao.deleteByPrimaryKeys(ids); // 批量删除，需要自定义该方法
        }
    }

    /**
     * 模板中下拉选择框的使用
     * @return
     */
    @Override
    public List<Map<String, String>> selectOptionList() {

        return brandDao.selectOptionList();
    }
}
