package com.swapu.aspect;

import com.alibaba.fastjson.JSON;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 全局接口日志切面，记录所有 Controller 接口的请求和响应信息
 */
@Aspect
@Component
public class GlobalLogAspect {

    private static final Logger log = LoggerFactory.getLogger(GlobalLogAspect.class);

    // 拦截 com.swapu.controller 包下所有的 public 方法
    @Pointcut("execution(public * com.swapu.controller.*.*(..))")
    public void controllerLog() {}

    @Around("controllerLog()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        // 开始时间
        long startTime = System.currentTimeMillis();

        // 获取 Request
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = null;
        if (attributes != null) {
            request = attributes.getRequest();
        }

        String url = request != null ? request.getRequestURL().toString() : "Unknown URL";
        String httpMethod = request != null ? request.getMethod() : "Unknown Method";
        String classMethod = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();
        
        // 获取请求参数
        Object[] args = joinPoint.getArgs();
        String params = "";
        try {
            // 过滤掉 ServletRequest / ServletResponse 避免序列化报错
            Object[] filteredArgs = new Object[args.length];
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof jakarta.servlet.ServletRequest || 
                    args[i] instanceof jakarta.servlet.ServletResponse) {
                    filteredArgs[i] = "HttpServletRequest/Response";
                } else {
                    filteredArgs[i] = args[i];
                }
            }
            params = JSON.toJSONString(filteredArgs);
        } catch (Exception e) {
            params = "[包含无法序列化的对象]";
        }

        log.info("========================================== Start ==========================================");
        log.info("【请求 URL】 : {}", url);
        log.info("【HTTP 方法】: {}", httpMethod);
        log.info("【调用方法】 : {}", classMethod);
        log.info("【请求参数】 : {}", params);

        // 执行目标方法
        Object result = null;
        try {
            result = joinPoint.proceed();
        } catch (Throwable throwable) {
            log.error("【接口异常】 : ", throwable);
            throw throwable;
        } finally {
            // 结束时间
            long endTime = System.currentTimeMillis();
            long timeCost = endTime - startTime;
            
            String resultStr = "";
            try {
                if (result != null) {
                    if (result instanceof reactor.core.publisher.Flux) {
                        resultStr = "[Flux 流式响应]";
                    } else if (result instanceof reactor.core.publisher.Mono) {
                        resultStr = "[Mono 响应]";
                    } else {
                        resultStr = JSON.toJSONString(result);
                        // 如果返回值太长，截断打印以防刷屏
                        if (resultStr != null && resultStr.length() > 500) {
                            resultStr = resultStr.substring(0, 500) + "...(已截断)";
                        }
                    }
                } else {
                    resultStr = "null (可能为 void 方法或直接操作 Response)";
                }
            } catch (Exception e) {
                // 如果是 IllegalStateException，大概率是 getOutputStream() 已经被调用，属于正常情况
                if (e.getClass().getName().contains("IllegalStateException")) {
                    resultStr = "[Response 已经被业务代码提交]";
                } else {
                    resultStr = "[返回值无法序列化]";
                }
            }

            log.info("【返回结果】 : {}", resultStr);
            log.info("【接口耗时】 : {} ms", timeCost);
            log.info("=========================================== End ===========================================");
        }

        return result;
    }
}
