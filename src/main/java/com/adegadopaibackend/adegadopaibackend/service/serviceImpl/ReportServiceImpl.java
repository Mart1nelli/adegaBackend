package com.adegadopaibackend.adegadopaibackend.service.serviceImpl;

import com.adegadopaibackend.adegadopaibackend.dto.request.CreateReportRequest;
import com.adegadopaibackend.adegadopaibackend.dto.response.ReportResponse;
import com.adegadopaibackend.adegadopaibackend.entity.Report;
import com.adegadopaibackend.adegadopaibackend.mapper.ReportMapper;
import com.adegadopaibackend.adegadopaibackend.repository.ReportRepository;
import com.adegadopaibackend.adegadopaibackend.service.ReportService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final ReportMapper reportMapper;

    @Override
    public ReportResponse create(CreateReportRequest req) {
        Report report = reportMapper.toEntity(req);
        Report saved = reportRepository.save(report);
        return reportMapper.toResponse(saved);
    }

    @Override
    public List<ReportResponse> findAll() {
        return reportMapper.toResponseList(reportRepository.findAll());
    }

    @Override
    public ReportResponse findById(Long id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Report with ID: " + id + " not found"));
        return reportMapper.toResponse(report);
    }

    @Override
    public List<ReportResponse> findByType(String type) {
        return reportMapper.toResponseList(reportRepository.findByTypeOrderByGeneratedAtDesc(type));
    }
}
