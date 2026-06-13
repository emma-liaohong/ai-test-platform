package com.aitest.modules.defect.service.impl;

import com.aitest.common.exception.BusinessException;
import com.aitest.common.result.PageResult;
import com.aitest.modules.defect.dto.DefectDTO;
import com.aitest.modules.defect.dto.DefectQueryDTO;
import com.aitest.modules.defect.entity.Defect;
import com.aitest.modules.defect.mapper.DefectMapper;
import com.aitest.modules.defect.service.DefectService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefectServiceImpl extends ServiceImpl<DefectMapper, Defect> implements DefectService {

    private static final Set<String> VALID_STATUSES = Set.of("OPEN", "FIXED", "VERIFIED", "CLOSED", "REOPENED");

    @Override
    public PageResult<Defect> listDefects(DefectQueryDTO query) {
        Page<Defect> page = new Page<>(query.getPage(), query.getSize());

        LambdaQueryWrapper<Defect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(query.getSystemId() != null, Defect::getSystemId, query.getSystemId())
               .eq(StringUtils.hasText(query.getSeverity()), Defect::getSeverity, query.getSeverity())
               .eq(StringUtils.hasText(query.getPriority()), Defect::getPriority, query.getPriority())
               .eq(StringUtils.hasText(query.getStatus()), Defect::getStatus, query.getStatus())
               .and(StringUtils.hasText(query.getKeyword()), w ->
                       w.like(Defect::getDefectCode, query.getKeyword())
                        .or()
                        .like(Defect::getDefectTitle, query.getKeyword()))
               .orderByDesc(Defect::getCreatedAt);

        Page<Defect> result = page(page, wrapper);
        return PageResult.of(
                result.getTotal(), result.getCurrent(),
                result.getSize(), result.getPages(), result.getRecords());
    }

    @Override
    public Defect getDefect(Long id) {
        Defect defect = getById(id);
        if (defect == null) {
            throw new BusinessException(404, "Defect not found: " + id);
        }
        return defect;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Defect createDefect(DefectDTO dto) {
        Defect defect = new Defect();
        defect.setDefectCode("BUG-" + System.currentTimeMillis());
        defect.setDefectTitle(dto.getDefectTitle());
        defect.setSystemId(dto.getSystemId());
        defect.setSeverity(dto.getSeverity());
        defect.setPriority(dto.getPriority());
        defect.setStatus(dto.getStatus() != null ? dto.getStatus() : "OPEN");
        defect.setRelatedCaseId(dto.getRelatedCaseId());
        defect.setRelatedExecutionId(dto.getRelatedExecutionId());
        defect.setDescription(dto.getDescription());
        defect.setStepsToReproduce(dto.getStepsToReproduce());
        defect.setExpectedResult(dto.getExpectedResult());
        defect.setActualResult(dto.getActualResult());
        defect.setEnvironment(dto.getEnvironment());
        defect.setScreenshots(dto.getScreenshots());
        defect.setAssignedTo(dto.getAssignedTo());
        save(defect);
        log.info("Defect created: {}", defect.getDefectCode());
        return defect;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Defect updateDefect(Long id, DefectDTO dto) {
        Defect defect = getDefect(id);
        if (StringUtils.hasText(dto.getDefectTitle())) {
            defect.setDefectTitle(dto.getDefectTitle());
        }
        if (dto.getSystemId() != null) {
            defect.setSystemId(dto.getSystemId());
        }
        if (dto.getSeverity() != null) {
            defect.setSeverity(dto.getSeverity());
        }
        if (dto.getPriority() != null) {
            defect.setPriority(dto.getPriority());
        }
        if (dto.getStatus() != null) {
            defect.setStatus(dto.getStatus());
        }
        if (dto.getRelatedCaseId() != null) {
            defect.setRelatedCaseId(dto.getRelatedCaseId());
        }
        if (dto.getRelatedExecutionId() != null) {
            defect.setRelatedExecutionId(dto.getRelatedExecutionId());
        }
        if (dto.getDescription() != null) {
            defect.setDescription(dto.getDescription());
        }
        if (dto.getStepsToReproduce() != null) {
            defect.setStepsToReproduce(dto.getStepsToReproduce());
        }
        if (dto.getExpectedResult() != null) {
            defect.setExpectedResult(dto.getExpectedResult());
        }
        if (dto.getActualResult() != null) {
            defect.setActualResult(dto.getActualResult());
        }
        if (dto.getEnvironment() != null) {
            defect.setEnvironment(dto.getEnvironment());
        }
        if (dto.getScreenshots() != null) {
            defect.setScreenshots(dto.getScreenshots());
        }
        if (dto.getAssignedTo() != null) {
            defect.setAssignedTo(dto.getAssignedTo());
        }
        updateById(defect);
        log.info("Defect updated: {}", defect.getDefectCode());
        return defect;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDefect(Long id) {
        Defect defect = getDefect(id);
        removeById(id);
        log.info("Defect deleted: {}", defect.getDefectCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Defect updateStatus(Long id, String status) {
        Defect defect = getDefect(id);
        if (!VALID_STATUSES.contains(status)) {
            throw new BusinessException(400, "Invalid defect status: " + status + ". Must be one of: OPEN, FIXED, VERIFIED, CLOSED, REOPENED");
        }
        defect.setStatus(status);
        updateById(defect);
        log.info("Defect {} status updated to: {}", defect.getDefectCode(), status);
        return defect;
    }

    @Override
    public Map<String, Object> getStatistics(Long systemId) {
        LambdaQueryWrapper<Defect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(systemId != null, Defect::getSystemId, systemId);
        List<Defect> allDefects = list(wrapper);

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalCount", allDefects.size());

        // By severity
        Map<String, Long> bySeverity = new LinkedHashMap<>();
        bySeverity.put("S1", allDefects.stream().filter(d -> "S1".equals(d.getSeverity())).count());
        bySeverity.put("S2", allDefects.stream().filter(d -> "S2".equals(d.getSeverity())).count());
        bySeverity.put("S3", allDefects.stream().filter(d -> "S3".equals(d.getSeverity())).count());
        bySeverity.put("S4", allDefects.stream().filter(d -> "S4".equals(d.getSeverity())).count());
        stats.put("bySeverity", bySeverity);

        // By status
        Map<String, Long> byStatus = new LinkedHashMap<>();
        byStatus.put("OPEN", allDefects.stream().filter(d -> "OPEN".equals(d.getStatus())).count());
        byStatus.put("FIXED", allDefects.stream().filter(d -> "FIXED".equals(d.getStatus())).count());
        byStatus.put("VERIFIED", allDefects.stream().filter(d -> "VERIFIED".equals(d.getStatus())).count());
        byStatus.put("CLOSED", allDefects.stream().filter(d -> "CLOSED".equals(d.getStatus())).count());
        byStatus.put("REOPENED", allDefects.stream().filter(d -> "REOPENED".equals(d.getStatus())).count());
        stats.put("byStatus", byStatus);

        // By priority
        Map<String, Long> byPriority = new LinkedHashMap<>();
        byPriority.put("P0", allDefects.stream().filter(d -> "P0".equals(d.getPriority())).count());
        byPriority.put("P1", allDefects.stream().filter(d -> "P1".equals(d.getPriority())).count());
        byPriority.put("P2", allDefects.stream().filter(d -> "P2".equals(d.getPriority())).count());
        byPriority.put("P3", allDefects.stream().filter(d -> "P3".equals(d.getPriority())).count());
        stats.put("byPriority", byPriority);

        return stats;
    }
}
