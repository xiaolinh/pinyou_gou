package cn.itcast.core.service.address;

import cn.itcast.core.pojo.address.Address;

import java.util.List;

public interface AddressService {

    /**
     * 回显账户中地址
     * @param username
     * @return
     */
    public List<Address> addressList(String username);
}
