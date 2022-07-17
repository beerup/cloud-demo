package cn.itcast.order.service;

import cn.itcast.order.pojo.Order;

/**
 * @author tsh
 * @version 1.0
 * @date 2022/7/7 16:10
 */
public interface OrderService {

    Order queryOrderById(Long orderId);

    default int count() {
        return 0;
    }
}
