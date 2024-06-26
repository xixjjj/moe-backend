package com.abdecd.moebackend.business.dao.mapper;

import com.abdecd.moebackend.business.dao.entity.UserManage;
import com.abdecd.moebackend.business.pojo.vo.plainuser.AllVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface PlainUserManageMapper extends BaseMapper<UserManage> {

    @Select("<script>" +
            "SELECT id, nickname, email, status, permission, create_time " +
            "FROM user " +
            "WHERE 1=1 " +
            "<if test='id != null'>AND id = #{id}</if> " +
            "<if test='name != null'>AND nickname LIKE CONCAT('%', #{name}, '%')</if> " +
            "<if test='status != null'>AND status = #{status}</if> " +
            "</script>")
    Page<AllVO> selectUsers(Page<?> page, @Param("id") Long id, @Param("name") String name, @Param("status") Integer status);
}
