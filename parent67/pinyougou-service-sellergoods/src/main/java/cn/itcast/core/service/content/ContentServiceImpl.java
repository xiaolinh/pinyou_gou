package cn.itcast.core.service.content;

import java.util.List;


import cn.itcast.core.pojo.ad.ContentQuery;
import cn.itcast.core.pojo.entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import cn.itcast.core.dao.ad.ContentDao;
import cn.itcast.core.pojo.ad.Content;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;


@Service
public class ContentServiceImpl implements ContentService {

	@Autowired
	private ContentDao contentDao;

	@Resource
	private RedisTemplate<String, Object> redisTemplate;

	@Override
	public List<Content> findAll() {
		List<Content> list = contentDao.selectByExample(null);
		return list;
	}

	@Override
	public PageResult findPage(Content content, Integer pageNum, Integer pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		Page<Content> page = (Page<Content>)contentDao.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public void add(Content content) {
		contentDao.insertSelective(content);
	}

	@Override
	public void edit(Content content) {
		contentDao.updateByPrimaryKeySelective(content);
	}

	@Override
	public Content findOne(Long id) {
		Content content = contentDao.selectByPrimaryKey(id);
		return content;
	}

	@Override
	public void delAll(Long[] ids) {
		if(ids != null){
			for(Long id : ids){
				contentDao.deleteByPrimaryKey(id);
			}
		}
	}

	/**
	 * 首页大广告的轮播
	 * @param categoryId
	 * @return
	 */
    @Override
    public List<Content> findByCategoryId(Long categoryId) {
		List<Content> list = (List<Content>) redisTemplate.boundHashOps("CONTENT").get(categoryId);
		//加锁,加入队列,进行二次判断,防止缓存穿透.
		synchronized (this) {
			if (list == null) {
				//二次判断
				list = (List<Content>) redisTemplate.boundHashOps("CONTENT").get(categoryId);
				if (list == null) {
					ContentQuery contentQuery = new ContentQuery();
					contentQuery.createCriteria().andCategoryIdEqualTo(categoryId);
					list = contentDao.selectByExample(contentQuery);
                    redisTemplate.boundHashOps("content").put(categoryId, list);
				}
			}
		}
		return list;
    }

}
