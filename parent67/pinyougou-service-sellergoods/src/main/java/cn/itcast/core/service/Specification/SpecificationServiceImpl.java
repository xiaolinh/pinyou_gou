package cn.itcast.core.service.Specification;

import cn.itcast.core.dao.specification.SpecificationDao;
import cn.itcast.core.dao.specification.SpecificationOptionDao;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.specification.Specification;
import cn.itcast.core.pojo.specification.SpecificationOption;
import cn.itcast.core.pojo.specification.SpecificationOptionQuery;
import cn.itcast.core.pojo.specification.SpecificationQuery;
import cn.itcast.core.pojo.vo.SpecificationVo;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
public class SpecificationServiceImpl implements SpecificationService {

    @Resource
    private SpecificationDao specificationDao;
    @Resource
    private SpecificationOptionDao specificationOptionDao;
    /**
     * 搜索查询
     * @param page
     * @param rows
     * @param specification
     * @return
     */
    @Override
    public PageResult<Specification> search(Integer page, Integer rows, Specification specification) {
        //设置分页助手
        PageHelper.startPage(page,rows);
        //设置查询条件
        SpecificationQuery specificationQuery = new SpecificationQuery();
        SpecificationQuery.Criteria criteria = specificationQuery.createCriteria();
        if (specification.getSpecName() != null && !"".equals(specification.getSpecName().trim())) {
            criteria.andSpecNameLike("%" + specification.getSpecName() + "%");
        }
        //降序查询
        specificationQuery.setOrderByClause("id desc");
        //开始查询
        Page<Specification> page1 = (Page<Specification>) specificationDao.selectByExample(specificationQuery);

        return new PageResult<Specification>(page1.getTotal(),page1.getResult());
    }

    /**
     * 添加商品规格
     * @param specificationVo
     */
    @Override
    public void add(SpecificationVo specificationVo) {
        Specification specification = specificationVo.getSpecification();
        specificationDao.insertSelective(specification);
        List<SpecificationOption> specificationVoList = specificationVo.getSpecificationOptionList();
        if (specificationVoList !=null && specificationVoList.size()>0) {
            for (SpecificationOption specificationOption : specificationVoList) {
                    specificationOption.setSpecId(specification.getId());
            }
            specificationOptionDao.insertSelectives(specificationVoList);
        }

    }

    /**
     * 查询实体(修改规格的数据会回显)
     * @param id
     * @return
     */
    @Override
    public SpecificationVo findOne(Long id) {
        //查询规格
        Specification specification = specificationDao.selectByPrimaryKey(id);
        //查询规格选项
        SpecificationOptionQuery specificationOptionQuery = new SpecificationOptionQuery();
        SpecificationOptionQuery.Criteria criteria = specificationOptionQuery.createCriteria();
        criteria.andSpecIdEqualTo(id);
        List<SpecificationOption> specificationOptions = specificationOptionDao.selectByExample(specificationOptionQuery);

        return new SpecificationVo(specification,specificationOptions);
    }

    /**
     * 修改规则
     * @param specificationVo
     */
    @Override
    public void update(SpecificationVo specificationVo) {
        //修改规则名称
        Specification specification = specificationVo.getSpecification();
        specificationDao.updateByPrimaryKeySelective(specification);


        List<SpecificationOption> optionList = specificationVo.getSpecificationOptionList();
        //删除规则选项
        SpecificationOptionQuery optionQuery = new SpecificationOptionQuery();
        SpecificationOptionQuery.Criteria criteria = optionQuery.createCriteria();
        criteria.andSpecIdEqualTo(specification.getId());
        specificationOptionDao.deleteByExample(optionQuery);
        //添加规则选项
        if (optionList !=null && optionList.size()>0) {
            for (SpecificationOption specificationOption : optionList) {
                specificationOption.setSpecId(specification.getId());
            }
            specificationOptionDao.insertSelectives(optionList);
        }
    }

    /**
     * 删除规格模板
     * @param ids
     */
    @Override
    public void delete(Long[] ids) {
        if(ids != null && ids.length > 0){
            for (Long id : ids) {
                // 主从表：先删除从表在删除主表
                // 删除规格选项
                SpecificationOptionQuery query = new SpecificationOptionQuery();
                query.createCriteria().andSpecIdEqualTo(id);
                specificationOptionDao.deleteByExample(query);
                // 删除规格
                specificationDao.deleteByPrimaryKey(id);
            }
        }
    }

    @Override
    public List<Map<String, String>> selectOptionList() {
        return specificationDao.selectOptionList();
    }
}
