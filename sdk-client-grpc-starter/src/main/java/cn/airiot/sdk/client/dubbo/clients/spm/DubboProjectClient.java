package cn.airiot.sdk.client.dubbo.clients.spm;

import cn.airiot.sdk.client.builder.Query;
import cn.airiot.sdk.client.dto.InsertResult;
import cn.airiot.sdk.client.dto.Response;
import cn.airiot.sdk.client.dubbo.grpc.api.CreateRequest;
import cn.airiot.sdk.client.dubbo.grpc.api.GetOrDeleteRequest;
import cn.airiot.sdk.client.dubbo.grpc.api.QueryRequest;
import cn.airiot.sdk.client.dubbo.grpc.api.UpdateRequest;
import cn.airiot.sdk.client.dubbo.grpc.spm.DubboProjectServiceGrpc;
import cn.airiot.sdk.client.dubbo.utils.DubboClientUtils;
import cn.airiot.sdk.client.service.spm.ProjectClient;
import cn.airiot.sdk.client.service.spm.dto.LicenseContent;
import cn.airiot.sdk.client.service.spm.dto.Project;
import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import java.util.List;


@Component
public class DubboProjectClient implements ProjectClient {

    private final Logger logger = LoggerFactory.getLogger(DubboProjectClient.class);

    private final DubboProjectServiceGrpc.IProjectService projectService;

    public DubboProjectClient(DubboProjectServiceGrpc.IProjectService projectService) {
        this.projectService = projectService;
    }

    @Override
    public Response<InsertResult> create(@Nonnull Project project) {
        if (logger.isDebugEnabled()) {
            logger.debug("创建项目: project = {}", project);
        }

        ByteString projectData = DubboClientUtils.serialize(project);

        if (logger.isTraceEnabled()) {
            logger.trace("创建项目: project = {}", projectData.toStringUtf8());
        }

        cn.airiot.sdk.client.dubbo.grpc.api.Response response = this.projectService.create(CreateRequest.newBuilder()
                .setData(projectData)
                .build());

        if (logger.isDebugEnabled()) {
            logger.debug("创建项目: project = {}, response = {}", project, DubboClientUtils.toString(response));
        }

        return DubboClientUtils.deserialize(InsertResult.class, response);
    }

    @Override
    public Response<List<Project>> query(@Nonnull Query query) {
        byte[] queryData = query.serialize();

        if (logger.isDebugEnabled()) {
            logger.debug("查询项目: query = {}", new String(queryData));
        }

        cn.airiot.sdk.client.dubbo.grpc.api.Response response = this.projectService.query(QueryRequest.newBuilder()
                .setQuery(ByteString.copyFrom(queryData))
                .build());

        if (logger.isDebugEnabled()) {
            logger.debug("查询项目: query = {}, response = {}", new String(queryData), DubboClientUtils.toString(response));
        }

        return DubboClientUtils.deserializeList(Project.class, response);
    }

    @Override
    public Response<List<Project>> queryAll() {
        byte[] queryData = Query.newBuilder()
                .select(Project.class)
                .build()
                .serialize();

        if (logger.isDebugEnabled()) {
            logger.debug("查询全部项目: query = {}", new String(queryData));
        }

        cn.airiot.sdk.client.dubbo.grpc.api.Response response = this.projectService.query(QueryRequest.newBuilder()
                .setQuery(ByteString.copyFrom(queryData))
                .build());

        if (logger.isDebugEnabled()) {
            logger.debug("查询全部项目: query = {}, response = {}", new String(queryData), DubboClientUtils.toString(response));
        }

        return DubboClientUtils.deserializeList(Project.class, response);
    }

    @Override
    public Response<Project> queryById(@Nonnull String projectId) {
        if (!StringUtils.hasText(projectId)) {
            throw new IllegalArgumentException("'projectId' cannot be null or empty");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("查询项目: projectId = {}", projectId);
        }

        cn.airiot.sdk.client.dubbo.grpc.api.Response response = this.projectService.get(GetOrDeleteRequest.newBuilder()
                .setId(projectId)
                .build());

        if (logger.isDebugEnabled()) {
            logger.debug("查询项目: projectId = {}, response = {}", projectId, DubboClientUtils.toString(response));
        }

        return DubboClientUtils.deserialize(Project.class, response);
    }

    @Override
    public Response<Void> update(@Nonnull Project project) {
        if (!StringUtils.hasText(project.getId())) {
            throw new IllegalArgumentException("the project id cannot be null or empty");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("更新项目: projectId = {}, project = {}", project.getId(), project);
        }

        ByteString projectData = DubboClientUtils.serializeWithoutId(project);

        if (logger.isTraceEnabled()) {
            logger.debug("更新项目: projectId = {}, project = {}", project.getId(), projectData.toStringUtf8());
        }

        cn.airiot.sdk.client.dubbo.grpc.api.Response response = this.projectService.update(
                UpdateRequest.newBuilder()
                        .setId(project.getId())
                        .setData(projectData)
                        .build()
        );

        if (logger.isDebugEnabled()) {
            logger.debug("更新项目: projectId = {}, project = {}, response = {}",
                    project.getId(), projectData.toStringUtf8(), DubboClientUtils.toString(response));
        }

        return DubboClientUtils.deserialize(Void.class, response);
    }

    @Override
    public Response<Void> updateLicense(@Nonnull String projectId, @Nonnull LicenseContent license) {
        if (!StringUtils.hasText(projectId)) {
            throw new IllegalArgumentException("the project id cannot be null or empty");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("更新项目授权信息: projectId = {}, license = {}", projectId, license);
        }

        ByteString licenseData = DubboClientUtils.serialize(license);

        if (logger.isTraceEnabled()) {
            logger.debug("更新项目授权信息: projectId = {}, license = {}", projectId, licenseData.toStringUtf8());
        }

        cn.airiot.sdk.client.dubbo.grpc.api.Response response = this.projectService.updateLicense(
                UpdateRequest.newBuilder()
                        .setId(projectId)
                        .setData(licenseData)
                        .build()
        );

        if (logger.isDebugEnabled()) {
            logger.debug("更新项目授权信息: projectId = {}, license = {}, response = {}",
                    projectId, licenseData.toStringUtf8(), DubboClientUtils.toString(response));
        }

        return DubboClientUtils.deserialize(Void.class, response);
    }

    @Override
    public Response<Void> replace(@Nonnull Project project) {
        if (!StringUtils.hasText(project.getId())) {
            throw new IllegalArgumentException("the project id cannot be null or empty");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("替换项目: projectId = {}, project = {}", project.getId(), project);
        }

        ByteString projectData = DubboClientUtils.serializeWithoutId(project);

        if (logger.isTraceEnabled()) {
            logger.debug("替换项目: projectId = {}, project = {}", project.getId(), projectData.toStringUtf8());
        }

        cn.airiot.sdk.client.dubbo.grpc.api.Response response = this.projectService.replace(
                UpdateRequest.newBuilder()
                        .setId(project.getId())
                        .setData(projectData)
                        .build()
        );

        if (logger.isDebugEnabled()) {
            logger.debug("替换项目: projectId = {}, project = {}, response = {}",
                    project.getId(), projectData.toStringUtf8(), DubboClientUtils.toString(response));
        }

        return DubboClientUtils.deserialize(Void.class, response);
    }

    @Override
    public Response<Void> deleteById(@Nonnull String projectId) {
        if (!StringUtils.hasText(projectId)) {
            throw new IllegalArgumentException("'projectId' cannot be null or empty");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("删除项目: projectId = {}", projectId);
        }

        cn.airiot.sdk.client.dubbo.grpc.api.Response response = this.projectService.delete(GetOrDeleteRequest.newBuilder()
                .setId(projectId)
                .build());

        if (logger.isDebugEnabled()) {
            logger.debug("删除项目: projectId = {}, response = {}", projectId, DubboClientUtils.toString(response));
        }

        return DubboClientUtils.deserialize(Void.class, response);
    }
}
