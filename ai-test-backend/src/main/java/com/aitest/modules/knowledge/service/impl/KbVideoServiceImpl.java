package com.aitest.modules.knowledge.service.impl;

import com.aitest.common.exception.BusinessException;
import com.aitest.common.result.PageResult;
import com.aitest.modules.knowledge.entity.KbVideo;
import com.aitest.modules.knowledge.mapper.KbVideoMapper;
import com.aitest.modules.knowledge.service.KbVideoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class KbVideoServiceImpl extends ServiceImpl<KbVideoMapper, KbVideo> implements KbVideoService {

    @Override
    public PageResult<KbVideo> listVideos(int page, int size, Long systemId, String videoType) {
        Page<KbVideo> pageObj = new Page<>(page, size);

        LambdaQueryWrapper<KbVideo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(systemId != null, KbVideo::getSystemId, systemId)
               .eq(StringUtils.hasText(videoType), KbVideo::getVideoType, videoType)
               .orderByDesc(KbVideo::getCreatedAt);

        Page<KbVideo> result = page(pageObj, wrapper);
        return PageResult.of(
                result.getTotal(), result.getCurrent(), result.getSize(),
                result.getPages(), result.getRecords());
    }

    @Override
    public KbVideo createVideo(KbVideo video) {
        save(video);
        log.info("Video registered: {} (type={})", video.getVideoName(), video.getVideoType());
        return video;
    }

    @Override
    public void deleteVideo(Long id) {
        KbVideo video = getById(id);
        if (video == null) {
            throw new BusinessException(404, "Video not found: " + id);
        }
        removeById(id);
        log.info("Video deleted: {} ({})", id, video.getVideoName());
    }
}
