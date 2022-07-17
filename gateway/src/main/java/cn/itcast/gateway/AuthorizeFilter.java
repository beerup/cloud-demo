package cn.itcast.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

// @Order(-1)
@Slf4j
@Component
public class AuthorizeFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1.获取请求参数
        ServerHttpRequest request = exchange.getRequest();
        HttpHeaders headers = request.getHeaders();
        List<String> list = headers.get("authorization");
//        MultiValueMap<String, String> params = request.getQueryParams();
        // 2.获取参数中的 authorization 参数
//        String auth = params.getFirst("authorization");
        // 3.判断参数值是否等于 admin
        if (!CollectionUtils.isEmpty(list) && list.contains("admin")) {
            log.info("验证通过!authorization:{},请求路径:{}", list,request.getURI());
            // 4.是，放行
            return chain.filter(exchange);
        }
        log.info("验证失败!authorization:{},请求路径:{}", list,request.getURI());
        // 5.否，拦截
        // 5.1.设置状态码
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        // 5.2.拦截请求
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
