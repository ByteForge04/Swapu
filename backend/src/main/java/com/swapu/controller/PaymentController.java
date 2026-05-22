package com.swapu.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.swapu.common.Result;
import com.swapu.config.AlipayConfig;
import com.swapu.entity.TradeOrder;
import com.swapu.service.TradeOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PaymentController {

    @Autowired
    private AlipayConfig alipayConfig;

    @Autowired
    private AlipayClient alipayClient;

    @Autowired
    private TradeOrderService tradeOrderService;

    // 注入前端地址，默认为 localhost:3001 (开发环境)
    @org.springframework.beans.factory.annotation.Value("${frontend.url:http://localhost:3001}")
    private String frontendUrl;

    /**
     * 发起支付
     * @param orderId 订单ID
     */
    @GetMapping("/alipay")
    public void pay(@RequestParam Long orderId, HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=" + alipayConfig.getCharset());
        
        TradeOrder order = tradeOrderService.getById(orderId);
        if (order == null) {
            response.getWriter().write("订单不存在");
            response.getWriter().flush();
            return;
        }
        if (order.getPaymentStatus() != null && order.getPaymentStatus() == 1) {
            response.getWriter().write("订单已支付");
            response.getWriter().flush();
            return;
        }

        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(alipayConfig.getNotifyUrl());
        request.setReturnUrl(alipayConfig.getReturnUrl());

        // 构造业务参数
        Map<String, Object> bizContent = new HashMap<>();
        bizContent.put("out_trade_no", order.getOrderNo());
        bizContent.put("total_amount", order.getAmount().toString());
        bizContent.put("subject", "SwapU订单-" + order.getOrderNo());
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
        
        try {
            request.setBizContent(com.alibaba.fastjson.JSON.toJSONString(bizContent));
            String form = alipayClient.pageExecute(request).getBody();
            
            response.getWriter().write(form);
            response.getWriter().flush();
        } catch (AlipayApiException e) {
            e.printStackTrace();
            response.getWriter().write("支付发起失败");
            response.getWriter().flush();
        }
    }

    /**
     * 支付宝同步回调
     */
    @GetMapping("/return")
    public void returnCallback(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("收到支付宝同步回调");
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        
        for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用
            //valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, valueStr);
        }
        
        try {
            // 验签
            boolean signVerified = AlipaySignature.rsaCheckV1(params, alipayConfig.getAlipayPublicKey(), 
                    alipayConfig.getCharset(), alipayConfig.getSignType()); //调用SDK验证签名
            
            System.out.println("同步回调验签结果: " + signVerified);

            if(signVerified) {
                //商户订单号
                String out_trade_no = new String(request.getParameter("out_trade_no").getBytes("ISO-8859-1"),"UTF-8");
            
                //支付宝交易号
                String trade_no = new String(request.getParameter("trade_no").getBytes("ISO-8859-1"),"UTF-8");

                QueryWrapper<TradeOrder> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("order_no", out_trade_no);
                TradeOrder order = tradeOrderService.getOne(queryWrapper);
                
                if (order != null) {
                    // 本地开发环境由于没有公网IP，支付宝的异步回调(notify)可能无法到达
                    // 因此在同步回调(return)中也调用一次 paySuccess 来保证状态更新
                    // paySuccess 内部已有幂等性校验，多次调用是安全的
                    tradeOrderService.paySuccess(out_trade_no, trade_no, LocalDateTime.now());

                    // 跳转到订单详情页
                    System.out.println("跳转至: " + frontendUrl + "/order/detail/" + order.getOrderId());
                    response.sendRedirect(frontendUrl + "/order/detail/" + order.getOrderId());
                } else {
                    response.sendRedirect(frontendUrl + "/order/list");
                }
            } else {
                System.out.println("同步回调验签失败");
                response.setContentType("text/html;charset=" + alipayConfig.getCharset());
                response.getWriter().write("验签失败");
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
            response.sendRedirect(frontendUrl + "/order/list");
        }
    }

    /**
     * 支付宝异步回调
     */
    @PostMapping("/notify")
    public String notify(HttpServletRequest request) {
        System.out.println("====== 收到支付宝异步通知 ======");
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        
        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }
        
        System.out.println("通知参数：" + params);
        
        try {
            // 验签
            boolean verify = AlipaySignature.rsaCheckV1(params, alipayConfig.getAlipayPublicKey(), 
                    alipayConfig.getCharset(), alipayConfig.getSignType());
            
            System.out.println("验签结果：" + verify);
            
            if (verify) {
                // 验证成功
                String outTradeNo = params.get("out_trade_no");
                String tradeNo = params.get("trade_no");
                String tradeStatus = params.get("trade_status");
                String gmtPayment = params.get("gmt_payment"); // yyyy-MM-dd HH:mm:ss
                
                System.out.println("订单号：" + outTradeNo + ", 交易状态：" + tradeStatus);
                
                if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                    LocalDateTime payTime = LocalDateTime.now();
                    if (gmtPayment != null) {
                        payTime = LocalDateTime.parse(gmtPayment, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    }
                    
                    // 更新订单状态
                    tradeOrderService.paySuccess(outTradeNo, tradeNo, payTime);
                    System.out.println("更新订单状态完成");
                }
                
                return "success";
            } else {
                return "failure";
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
            return "failure";
        }
    }
}
