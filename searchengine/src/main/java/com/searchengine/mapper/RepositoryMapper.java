package com.searchengine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.searchengine.entity.Repository;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 仓库Mapper接口
 */
@Mapper
public interface RepositoryMapper extends BaseMapper<Repository> {

    /**
     * 插入仓库信息
     * @param repository 仓库实体
     * @return 影响行数
     */
    @Insert("INSERT INTO repository (fullName, htmlUrl, readme, repositoryId) " +
            "VALUES (#{fullName}, #{htmlUrl}, #{readme}, #{repositoryId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Repository repository);

    /**
     * 批量插入仓库信息
     * @param repositories 仓库实体列表
     * @return 影响行数
     */
    @Insert("<script>" +
            "INSERT INTO repository (fullName, htmlUrl, readme, repositoryId) VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.fullName}, #{item.htmlUrl}, #{item.readme}, #{item.repositoryId})" +
            "</foreach>" +
            "</script>")
    int batchInsert(@Param("list") List<Repository> repositories);

    /**
     * 根据ID查询仓库信息
     * @param id 主键ID
     * @return 仓库实体
     */
    @Select("SELECT * FROM repository WHERE id = #{id}")
    Repository selectById(Long id);

    /**
     * 根据仓库ID查询仓库信息
     * @param repositoryId Gitee仓库ID
     * @return 仓库实体
     */
    @Select("SELECT * FROM repository WHERE repositoryId = #{repositoryId}")
    Repository selectByRepositoryId(Long repositoryId);

    /**
     * 根据仓库名称查询仓库信息
     * @param fullName 仓库完整名称
     * @return 仓库实体列表
     */
    @Select("SELECT * FROM repository WHERE fullName LIKE CONCAT('%', #{fullName}, '%')")
    List<Repository> selectByFullName(String fullName);

    /**
     * 更新仓库信息
     * @param repository 仓库实体
     * @return 影响行数
     */
    @Update("UPDATE repository SET fullName = #{fullName}, htmlUrl = #{htmlUrl}, " +
            "readme = #{readme}, repositoryId = #{repositoryId} WHERE id = #{id}")
    int update(Repository repository);

    /**
     * 删除仓库信息
     * @param id 主键ID
     * @return 影响行数
     */
    @Delete("DELETE FROM repository WHERE id = #{id}")
    int deleteById(Long id);

    /**
     * 根据ID列表查询仓库信息，并保持与输入ID列表相同的顺序
     * @param ids ID列表
     * @return 仓库实体列表
     */
    @Select("<script>" +
            "SELECT * FROM repository WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " ORDER BY FIELD(id, " +
            "<foreach collection='ids' item='id' separator=','>" +
            "#{id}" +
            "</foreach>" +
            ")" +
            "</script>")
    List<Repository> selectByIdsInOrder(@Param("ids") List<Long> ids);
}
