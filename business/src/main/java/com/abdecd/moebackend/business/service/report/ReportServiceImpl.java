package com.abdecd.moebackend.business.service.report;

import com.abdecd.moebackend.business.dao.entity.Report;
import com.abdecd.moebackend.business.dao.mapper.ReportMapper;
import com.abdecd.moebackend.business.pojo.dto.report.AddReportDTO;
import com.abdecd.moebackend.business.service.report.ReportService;
import com.abdecd.tokenlogin.common.context.UserContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private ReportMapper reportMapper;

    @Override
    public Long addReport(AddReportDTO addReportDTO) {
        Report report = new Report();
        BeanUtils.copyProperties(addReportDTO, report);
        report.setUserId(UserContext.getUserId()); // 假设 UserContext 提供当前用户ID
        report.setCreateTime(LocalDateTime.now());
        report.setStatus(0); // 设置初始状态为等待处理
        reportMapper.insert(report);
        return report.getId();
    }
}

