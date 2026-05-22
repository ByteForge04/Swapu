package com.swapu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swapu.common.Result;
import com.swapu.entity.Item;
import com.swapu.entity.Report;
import com.swapu.mapper.ItemMapper;
import com.swapu.mapper.ReportMapper;
import com.swapu.service.ReportService;
import com.swapu.service.ItemService;
import com.swapu.utils.DeepSeekUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class ReportServiceImpl extends ServiceImpl<ReportMapper, Report> implements ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportServiceImpl.class);

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private DeepSeekUtils deepSeekUtils;

    @Autowired
    private ItemService itemService;

    @Override
    public Result<?> createReport(Report report) {
        // 检查参数
        if (report.getReason() == null || report.getReason().isEmpty()) {
            return Result.error("举报原因不能为空");
        }
        
        report.setStatus(0); // 默认待处理
        
        // 如果举报的是物品，可以先调用 AI 进行初步判定
        if (report.getType() != null && report.getType() == 1 && report.getTargetId() != null) {
            Item item = itemMapper.selectById(report.getTargetId());
            if (item != null) {
                try {
                    log.info("Start AI check for report. Item Title: {}, Report Reason: {}", item.getTitle(), report.getReason());
                    String aiResultStr = deepSeekUtils.checkReportReasonable(item.getTitle(), item.getDescription(), report.getReason());
                    if (aiResultStr.startsWith("{")) {
                        JSONObject aiResult = JSON.parseObject(aiResultStr);
                        if (aiResult != null) {
                            boolean isValid = aiResult.getBooleanValue("valid");
                            String aiReason = aiResult.getString("reason");
                            
                            if (isValid) {
                                // AI 判定合理：直接下架物品，并标记举报为已处理
                                report.setStatus(1); // 已处理
                                report.setResult("AI自动判定：举报属实，已自动下架违规物品。" + aiReason);
                                
                                // 下架物品
                                item.setStatus(4); // 违规下架
                                itemMapper.updateById(item);
                                itemService.deleteFromEs(item.getId());
                                log.info("AI auto-processed report. Item {} marked as taken down.", item.getId());
                            } else {
                                // AI 判定不合理：直接驳回举报
                                report.setStatus(2); // 已驳回
                                report.setResult("AI自动判定：举报不属实，已驳回。" + aiReason);
                                log.info("AI auto-rejected report on Item {}.", item.getId());
                            }
                            
                            report.setReason(report.getReason() + "\n\n【AI判定分析】" + aiReason);
                        }
                    } else {
                        log.warn("AI returned non-JSON format for report check: {}", aiResultStr);
                    }
                } catch (Exception e) {
                    log.error("AI check for report failed: {}", e.getMessage(), e);
                }
            }
        }
        
        report.setCreatedAt(LocalDateTime.now());
        report.setUpdatedAt(LocalDateTime.now());
        save(report);
        
        return Result.success();
    }

    @Override
    public Result<Page<Report>> listReports(Integer page, Integer size, Integer status) {
        Page<Report> pageParam = new Page<>(page, size);
        QueryWrapper<Report> queryWrapper = new QueryWrapper<>();
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        queryWrapper.orderByDesc("created_at");
        return Result.success(page(pageParam, queryWrapper));
    }

    @Override
    public Result<?> handleReport(Long reportId, Integer status, String result) {
        Report report = getById(reportId);
        if (report == null) {
            return Result.error("举报不存在");
        }
        
        report.setStatus(status);
        report.setResult(result);
        report.setUpdatedAt(LocalDateTime.now());
        updateById(report);
        
        return Result.success();
    }

    @Override
    public Result<Map<String, Object>> getReportDetail(Long reportId) {
        Report report = getById(reportId);
        if (report == null) {
            return Result.error("举报不存在");
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("report", report);
        
        // 如果是违规物品举报，获取物品信息
        if (report.getType() == 1) {
            Item item = itemMapper.selectById(report.getTargetId());
            data.put("item", item);
        }
        
        return Result.success(data);
    }
}
