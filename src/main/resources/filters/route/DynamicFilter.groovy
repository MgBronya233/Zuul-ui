package filters.route

import com.netflix.zuul.ZuulFilter
import com.netflix.zuul.exception.ZuulException
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants

class DynamicFilter extends ZuulFilter {

    @Override
    String filterType() {
        return FilterConstants.ROUTE_TYPE
    }

    @Override
    int filterOrder() {
        return 0
    }

    @Override
    boolean shouldFilter() {
        return true
    }

    @Override
    Object run() throws ZuulException {
        System.out.println("=========  这一个是动态加载的过滤器：DynamicFilter")
        return null
    }
}
