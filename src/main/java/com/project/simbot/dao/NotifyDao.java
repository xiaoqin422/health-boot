package com.project.simbot.dao;

import com.project.simbot.entity.Notify;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

/**
 * (Notify)表数据库访问层
 *
 * @author 秦笑笑
 * @since 2022-06-19 17:02:08
 */
public interface NotifyDao {
    List<String> searchByGroupCode(String groupCode);
    Notify searchStuCode(String stuNumber);
    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    Notify selectById(Integer id);
	
    /**
     * 分页查询
     *
     * @param start 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<Notify> selectPage(@Param("start") int start, @Param("limit") int limit);

    /**
     * 查询全部
     *
     * @return 对象列表
     */
    List<Notify> selectAll();
    
    /**
     * 通过实体作为筛选条件查询
     *
     * @param notify 实例对象
     * @return 对象列表
     */
    List<Notify> selectList(Notify notify);

    /**
     * 新增数据
     *
     * @param notify 实例对象
     * @return 影响行数
     */
    int insert(Notify notify);
	
	/**
     * 批量新增
     *
     * @param notifys 实例对象的集合
     * @return 影响行数
     */
	int batchInsert(List<Notify> notifys);
	
    /**
     * 修改数据
     *
     * @param notify 实例对象
     * @return 影响行数
     */
    int update(Notify notify);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Integer id);

    /**
     * 查询总数据数
     *
     * @return 数据总数
     */
    int count();
}

