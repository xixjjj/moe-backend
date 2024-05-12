package com.abdecd.moebackend.business.controller.backstage;

import com.abdecd.moebackend.business.common.exception.BaseException;
import com.abdecd.moebackend.business.dao.entity.VideoGroup;
import com.abdecd.moebackend.business.pojo.dto.backstage.commonVideoGroup.VideoGroupDTO;
import com.abdecd.moebackend.business.pojo.vo.backstage.commonVideoGroup.VideoGroupVO;
import com.abdecd.moebackend.business.pojo.vo.backstage.commonVideoGroup.VideoVo;
import com.abdecd.moebackend.business.service.backstage.VideoGroupAndTagService;
import com.abdecd.moebackend.business.service.backstage.VideoGroupService;
import com.abdecd.moebackend.common.result.Result;
import com.abdecd.tokenlogin.aspect.RequirePermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

@Tag(name = "普通视频组接口")
@Slf4j
@RestController
@RequestMapping("/backstage/plain-video-group")
public class PlainVideoGroupController {

    @Resource
    private VideoGroupService videoGroupService;

    @Resource
    private VideoGroupAndTagService videoGroupAndTagService;

    @RequirePermission(value = "99", exception = BaseException.class)
    @Operation(summary = "普通视频组添加", description = "data字段返回新增普通视频组id")
    @PostMapping(value = "/add", consumes = "multipart/form-data")
    @ResponseBody
    public Result<Long> addVideoGroup(@Valid VideoGroupDTO videoGroupDTO) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");
        LocalDateTime ldt = LocalDateTime.now();

        Long groupId = videoGroupService.insert(new VideoGroup()
                .setCreateTime(ldt)
                .setTitle(videoGroupDTO.getTitle())
                .setDescription(videoGroupDTO.getDescription())
                ,videoGroupDTO.getCover()
            );

        if (videoGroupDTO.getTagIds() != null) {
            for (String i : videoGroupDTO.getTagIds()) {
                Long tagId = Long.valueOf(i);
                videoGroupAndTagService.insert(tagId, groupId);
            }
        }

        return Result.success(groupId);
    }

    @RequirePermission(value = "99", exception = BaseException.class)
    @Operation(summary = "普通视频组删除")
    @PostMapping(value = "/delete")
    public Result<String> delVideoGroup(@Valid @RequestParam("id") Long id) {
        videoGroupService.delete(id);
        return Result.success();
    }

    @RequirePermission(value = "99", exception = BaseException.class)
    @Operation(summary = "普通视频组更新")
    @PostMapping(value = "/update", consumes = "multipart/form-data")
    @ResponseBody
    @CacheEvict(cacheNames = "videoGroup", key = "#videoGroupDTO.id")
    public Result<String> updateVideoGroup(@Valid VideoGroupDTO videoGroupDTO) {
        videoGroupService.update(videoGroupDTO);
        return Result.success();
    }

    @RequirePermission(value = "99", exception = BaseException.class)
    @Operation(summary = "普通视频组获取", description = "data字段返回普通视频组信息")
    @GetMapping("")
    public Result<VideoGroupVO> getVideoGroup(@Valid @RequestParam("id") Long id) {
        VideoGroupVO videoGroupVO = videoGroupService.getById(id);
        return Result.success(videoGroupVO);
    }

    @RequirePermission(value = "99", exception = BaseException.class)
    @Operation(summary = "普通视频组目录获取", description = "data字段返回普通视频组目录")
    @GetMapping("/contents")
    public Result<ArrayList<VideoVo>> getVideoGroupContent(@Valid @RequestParam("id") Long id) {
        ArrayList<VideoVo> videoVoList = videoGroupService.getContentById(id);
        return Result.success(videoVoList);
    }
}