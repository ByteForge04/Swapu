package com.swapu.config;

import com.swapu.interceptor.AdminInterceptor;
import com.swapu.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-path}")
    private String uploadPath;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor())
                .addPathPatterns("/**") // 拦截所有请求
                .excludePathPatterns(
                        "/user/login",      // 放行登录接口
                        "/user/register",   // 放行注册接口
                        "/item/list",       // 放行物品列表接口 (游客可见)
                        "/item/detail/**",  // 放行物品详情接口
                        "/item-message/list/**", // 放行物品留言列表接口
                        "/announcement/list", // 放行公告列表接口
                        "/ws/**",           // 放行 WebSocket
                        "/ai/**",           // 放行 AI 对话接口 (包含流式)
                        "/common/upload",   // 放行上传接口
                        "/files/**",        // 放行静态资源
                        "/pay/notify",      // 放行支付宝回调
                        "/pay/return",      // 放行支付宝同步回调
                        "/error"            // 放行错误页面
                );

        // 管理员拦截器
        registry.addInterceptor(new AdminInterceptor())
                .addPathPatterns("/admin/**")
                .addPathPatterns("/category/add", "/category/update", "/category/delete/**")
                .addPathPatterns("/announcement/add", "/announcement/update", "/announcement/delete/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射 /files/** 到本地上传目录
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + uploadPath);
    }
}
