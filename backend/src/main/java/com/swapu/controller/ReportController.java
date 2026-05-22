package com.swapu.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.swapu.common.Result;
import com.swapu.entity.Report;
import com.swapu.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @PostMapping("/create")
    public Result<?> create(@RequestBody Report report, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        report.setReporterId(userId);
        return reportService.createReport(report);
    }

    // 管理员接口：获取举报列表
    @GetMapping("/admin/list")
    public Result<Page<Report>> list(@RequestParam(defaultValue = "1") Integer page,
                                     @RequestParam(defaultValue = "10") Integer size,
                                     @RequestParam(required = false) Integer status) {
        return reportService.listReports(page, size, status);
    }

    // 管理员接口：获取举报详情
    @GetMapping("/admin/detail/{reportId}")
    public Result<?> detail(@PathVariable Long reportId) {
        return reportService.getReportDetail(reportId);
    }

    // 管理员接口：处理举报
    @PostMapping("/admin/handle")
    public Result<?> handle(@RequestBody Report report) {
        return reportService.handleReport(report.getReportId(), report.getStatus(), report.getResult());
    }
}
