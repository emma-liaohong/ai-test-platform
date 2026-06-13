package com.aitest.modules.knowledge.service;

import com.aitest.common.result.PageResult;
import com.aitest.modules.knowledge.entity.KbVideo;
import com.baomidou.mybatisplus.extension.service.IService;

public interface KbVideoService extends IService<KbVideo> {

    /**
     * Paginated video listing with optional filters.
     */
    PageResult<KbVideo> listVideos(int page, int size, Long systemId, String videoType);

    /**
     * Register a new video record.
     */
    KbVideo createVideo(KbVideo video);

    /**
     * Delete a video record.
     */
    void deleteVideo(Long id);
}
