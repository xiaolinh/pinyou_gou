package cn.itcast.core.pojo.entity;


import cn.itcast.core.pojo.good.Brand;

import java.io.Serializable;
import java.util.List;

public class PageResult<T> implements Serializable {
    private Long total;//总数量
    private List<T> rows;//查询到的所有数据

    public PageResult(Long total, List<T> rows) {
        this.total = total;
        this.rows = rows;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public List<T> getRows() {
        return rows;
    }

    public void setRows(List<T> rows) {
        this.rows = rows;
    }
}
