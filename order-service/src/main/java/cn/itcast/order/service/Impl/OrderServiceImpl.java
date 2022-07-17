package cn.itcast.order.service.Impl;

import cn.itcast.feign.clients.UserClient;
import cn.itcast.order.mapper.OrderMapper;
import cn.itcast.order.pojo.Order;
import cn.itcast.order.service.OrderService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private UserClient userClient;

    @Override
    public Order queryOrderById(Long orderId) {
        List<String> list = new ArrayList<>();
        // 1.查询订单
        Order order = orderMapper.findById(orderId);
        // 2.用Feign远程调用
//         User user = userClient.findById(order.getUserId());
        // // 3.封装user到Order
//         order.setUser(user);
        // 4.返回
        return order;
    }

//    @Override
//    public int count() {
//        return 1;
//    }

    /*@Autowired
    private RestTemplate restTemplate;

    public Order queryOrderById(Long orderId) {
        // 1.查询订单
        Order order = orderMapper.findById(orderId);
        // 2.利用RestTemplate发起http请求，查询用户
        // 2.1.url路径
        String url = "http://userservice/user/" + order.getUserId();
        // 2.2.发送http请求，实现远程调用
        User user = restTemplate.getForObject(url, User.class);
        // 3.封装user到Order
        order.setUser(user);
        // 4.返回
        return order;
    }*/
}
