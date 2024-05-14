package com.abdecd.moebackend.business.service.videogroup;

import com.abdecd.moebackend.business.common.util.SpringContextUtil;
import com.abdecd.moebackend.business.dao.entity.BangumiVideoGroup;
import com.abdecd.moebackend.business.dao.entity.Video;
import com.abdecd.moebackend.business.dao.entity.VideoGroup;
import com.abdecd.moebackend.business.dao.entity.VideoGroupAndTag;
import com.abdecd.moebackend.business.dao.mapper.*;
import com.abdecd.moebackend.business.pojo.vo.plainuser.UploaderVO;
import com.abdecd.moebackend.business.pojo.vo.videogroup.BangumiVideoGroupVO;
import com.abdecd.moebackend.business.pojo.vo.videogroup.ContentsItemVO;
import com.abdecd.moebackend.business.pojo.vo.videogroup.BangumiVideoGroupTimeScheduleVO;
import com.abdecd.moebackend.business.service.PlainUserService;
import com.abdecd.moebackend.common.constant.MessageConstant;
import com.abdecd.moebackend.common.constant.RedisConstant;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class BangumiVideoGroupServiceBase {
    @Autowired
    private VideoGroupMapper videoGroupMapper;
    @Autowired
    private BangumiVideoGroupMapper bangumiVideoGroupMapper;
    @Autowired
    private PlainUserService plainUserService;
    @Autowired
    private VideoGroupAndTagMapper videoGroupAndTagMapper;
    @Autowired
    private VideoGroupTagMapper videoGroupTagMapper;
    @Autowired
    private VideoMapper videoMapper;

    @Cacheable(cacheNames = RedisConstant.BANGUMI_VIDEO_GROUP_CACHE, key = "#videoGroupId", unless = "#result == null")
    public BangumiVideoGroupVO getVideoGroupInfo(Long videoGroupId) {
        var base = videoGroupMapper.selectById(videoGroupId);
        if (base == null || !Objects.equals(base.getType(), VideoGroup.Type.ANIME_VIDEO_GROUP)) return null;
        var appendix = bangumiVideoGroupMapper.selectOne(new LambdaQueryWrapper<BangumiVideoGroup>()
                .eq(BangumiVideoGroup::getVideoGroupId, videoGroupId)
        );
        if (appendix == null) return null;
        // 为空是管理员
        var uploader = plainUserService.getPlainUserDetail(base.getUserId());
        var uploaderVO = uploader == null
                ? new UploaderVO()
                    .setId(null)
                    .setNickname(MessageConstant.ADMIN)
                    .setAvatar(MessageConstant.ADMIN_AVATAR)
                : new UploaderVO()
                    .setId(uploader.getUserId())
                    .setNickname(uploader.getNickname())
                    .setAvatar(uploader.getAvatar());
        var tagIds = videoGroupAndTagMapper.selectList(new LambdaQueryWrapper<VideoGroupAndTag>()
                .select(VideoGroupAndTag::getTagId)
                .eq(VideoGroupAndTag::getVideoGroupId, videoGroupId)
        );
        if (tagIds == null) tagIds = new ArrayList<>();
        var tags = videoGroupTagMapper.selectBatchIds(tagIds.stream().map(VideoGroupAndTag::getTagId).toList());

        var vo = new BangumiVideoGroupVO();
        BeanUtils.copyProperties(base, vo);
        BeanUtils.copyProperties(appendix, vo);
        vo.setUploader(uploaderVO);
        vo.setTags(tags);
        return vo;
    }

    @Cacheable(cacheNames = RedisConstant.BANGUMI_VIDEO_GROUP_CONTENTS_CACHE, key = "#videoGroupId", unless = "#result == null")
    public List<ContentsItemVO> getContents(Long videoGroupId) {
        var videoList = videoMapper.selectList(new LambdaQueryWrapper<Video>()
                .eq(Video::getVideoGroupId, videoGroupId)
        );
        return new ArrayList<>(
                videoList.stream().map(video -> new ContentsItemVO()
                        .setVideoId(video.getId())
                        .setVideoGroupId(video.getVideoGroupId())
                        .setIndex(video.getIndex())
                        .setTitle(video.getTitle())
                        .setVideoCover(video.getCover())
                ).toList()
        );
    }

    // todo
    @Cacheable(cacheNames = RedisConstant.BANGUMI_TIME_SCHEDULE_CACHE, key = "#date", unless = "#result == null")
    public List<BangumiVideoGroupTimeScheduleVO> getTimeSchedule(LocalDate date) {
        if (date.isBefore(LocalDate.now().minusDays(1))) return null;
        if (date.isAfter(LocalDate.now().plusDays(6))) return null;
        var ids = bangumiVideoGroupMapper.selectList(new LambdaQueryWrapper<BangumiVideoGroup>()
                .select(BangumiVideoGroup::getVideoGroupId)
                .last("order by RAND() limit " + ((int) (Math.random() * 5) + 1))
        );
        if (ids.isEmpty()) return new ArrayList<>();
        var videoGroupServiceBase = SpringContextUtil.getBean(VideoGroupServiceBase.class);
        return new ArrayList<>(ids.stream()
                .map(id -> videoGroupServiceBase.getVideoGroupWithData(id.getVideoGroupId()))
                .map(videoGroupWithDataVO -> new BangumiVideoGroupTimeScheduleVO()
                        .setVideoGroupWithDataVO(videoGroupWithDataVO)
                        .setWillUpdateTime(LocalTime.of(((int) (Math.random() * 3) + 3), 0, 0))
                        .setWillUpdateTitle("更新至第"+((int) (Math.random() * 5) + 2)+"话")
                )
                .toList()
        );
    }
}
