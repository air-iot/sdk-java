package cn.airiot.sdk.client.service.spm;


import cn.airiot.sdk.client.annotation.NonProject;
import cn.airiot.sdk.client.builder.Query;
import cn.airiot.sdk.client.dto.InsertResult;
import cn.airiot.sdk.client.dto.Response;
import cn.airiot.sdk.client.service.PlatformClient;
import cn.airiot.sdk.client.service.spm.dto.LicenseContent;
import cn.airiot.sdk.client.service.spm.dto.Project;

import java.util.List;

/**
 * 项目客户端, 用于对平台中的项目进行增删改查等操作
 */
@NonProject
public interface ProjectClient extends PlatformClient {

    /**
     * 创建项目
     *
     * @param project 项目信息
     * @return 项目信息或错误信息
     */
    Response<InsertResult> create(Project project);


    /**
     * 查询项目信息
     *
     * @param query 查询条件
     * @return 项目信息
     */
    Response<List<Project>> query(Query query);

    /**
     * 查询全部项目信息
     *
     * @return 项目信息
     */
    Response<List<Project>> queryAll();

    /**
     * 根据项目ID查询项目信息
     *
     * @param projectId 项目ID
     * @return 项目信息
     */
    Response<Project> queryById(String projectId);

    /**
     * 更新项目信息
     * <br>
     * 如果字段的值为 {@code null} 则不更新
     *
     * @param project 更新后的项目信息
     * @return 更新结果
     */
    Response<Void> update(Project project);

    /**
     * 更新项目的授权信息
     *
     * @param projectId 项目ID
     * @param license   授权信息
     * @return 更新结果
     */
    Response<Void> updateLicense(String projectId, LicenseContent license);

    /**
     * 替换项目信息
     *
     * @param project 替换后的项目信息
     * @return 替换结果
     */
    Response<Void> replace(Project project);

    /**
     * 删除项目
     *
     * @param projectId 项目ID
     * @return 删除结果
     */
    Response<Void> deleteById(String projectId);
}
