/**
 * @Author: Neo
 * @Date: 2022/12/05 星期一 19:43:40
 * @Project: javaweb_homework
 * @IDE: IntelliJ IDEA
 **/
package com.example.demo.filter;


import com.alibaba.fastjson2.JSON;
import com.example.demo.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {

    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String uri = request.getRequestURI();
        log.info("当前请求的uri为：{}", uri);

        //不需要处理的请求路径
        String[] urls = new String[]{
                "/user/login",
                "/user/loginByPassword",
                "/user/register",
                "/user/sendMsg",
                "/common/**",
                "/item/list",
                "/kind/list",
                "/user/logout",
                "/user/sendMail",
                "/user/regist",
                "/**/*.html",
                "/**/*.css",
                "/**/*.jsp",
                "/**/*.png",
                "/**/*.jpg",
                "/**/*.js",
                "/**/*.txt",
                "/**/*.asp",
                "/**/*.aspx",
                "/**/*.ashx",
                "/**/*.dll",
                "/**/*.php",
                "/**/*.gif",
                "/**/*.swf",
                "templates/**",
        };

        //判断本次请求是否需要处理
        if (check(urls, uri)) {
            log.info("本次请求{}不需要处理", uri);
            filterChain.doFilter(request, response);
            return;
        }

//        if (request.getSession().getAttribute("user") != null) {
//            log.info("用户已登录，用户id为{}", request.getSession().getAttribute("user"));
//
//            Long userId = (Long) request.getSession().getAttribute("user");
//            BaseContext.setCurrentId(userId);
//
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        //log.info("用户未登录，跳转到登录页面");
//        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
    }

    private boolean check(String[] urls, String uri) {
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, uri);
            if (match) {
                return true;
            }
        }
        return false;
    }
}
