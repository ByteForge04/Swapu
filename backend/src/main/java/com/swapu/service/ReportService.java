package com.swapu.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.swapu.common.Result;
import com.swapu.entity.Report;

public interface ReportService extends IService<Report> {
    /**
     * 提交举报
     */
    Result<?> createReport(Report report);

    /**
     * 分页查询举报列表 (管理员)
     */
    Result<Page<Report>> listReports(Integer page, Integer size, Integer status);

    /**
     * 处理举报
     */
    Result<?> handleReport(Long reportId, Integer status, String result);

    /**
     * 获取举报详情（包含关联物品/用户信息）
     */
    Result<java.util.Map<String, Object>> getReportDetail(Long reportId);
}
