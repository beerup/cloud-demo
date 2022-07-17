package cn.itcast.user.web;

import cn.itcast.user.pojo.User;
import cn.itcast.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/user")
//@RefreshScope
public class UserController {

//    @Value("${test}")
//    private String test;

    @Autowired
    private UserService userService;

    // @Value("${pattern.dateformat}")
    // private String dateformat;

//    @Autowired
//    private PatternProperties properties;
//
//    @GetMapping("prop")
//    public PatternProperties properties() {
//        return properties;
//    }

    @GetMapping("now")
    public String now() {
        // return LocalDateTime.now().format(DateTimeFormatter.ofPattern(properties.getDateformat()));
        return "";
    }

    /**
     * 路径： /user/110
     *
     * @param id 用户id
     * @return 用户
     */
    @GetMapping("/{id}")
    public User queryById(@PathVariable("id") Long id) {
        return userService.queryById(id);
    }
}
