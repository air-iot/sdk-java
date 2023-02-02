package cn.airiot.sdk.client.dubbo.configuration;


import cn.airiot.sdk.client.dubbo.clients.DubboProjectAuthorizationClient;
import cn.airiot.sdk.client.dubbo.clients.DubboTenantAuthorizationClient;
import cn.airiot.sdk.client.dubbo.grpc.core.*;
import cn.airiot.sdk.client.dubbo.grpc.datasource.DubboDataServiceGrpc;
import cn.airiot.sdk.client.dubbo.grpc.spm.DubboProjectServiceGrpc;
import cn.airiot.sdk.client.dubbo.utils.DubboClientUtils;
import cn.airiot.sdk.client.interceptor.EnableClientInterceptors;
import cn.airiot.sdk.client.properties.AuthorizationProperties;
import cn.airiot.sdk.client.properties.ClientProperties;
import cn.airiot.sdk.client.properties.ServiceConfig;
import cn.airiot.sdk.client.properties.ServiceType;
import cn.airiot.sdk.client.properties.condition.ConditionalOnServiceEnabled;
import cn.airiot.sdk.client.service.AuthorizationClient;
import org.apache.dubbo.common.constants.LoadbalanceRules;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.spring.ReferenceBean;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Dubbo 客户端配置类
 */
@EnableDubbo
@EnableAspectJAutoProxy
@EnableClientInterceptors
@EnableConfigurationProperties({ClientProperties.class, AuthorizationProperties.class})
@Configuration
public class DubboClientAutoConfiguration {

    /**
     * 项目级授权
     */
    @ConditionalOnProperty(prefix = "airiot.client.authorization", name = "type", havingValue = "project", matchIfMissing = true)
    @Configuration
    public static class ProjectAuthorizationConfiguration {
        /**
         * 扩展应用服务
         */
        @Bean
        @DubboReference(
                check = false,
                providedBy = "core",
                protocol = "grpc",
                retries = 3,
                timeout = 3000,
                scope = org.apache.dubbo.rpc.Constants.SCOPE_REMOTE,
                loadbalance = LoadbalanceRules.ROUND_ROBIN
        )
        public ReferenceBean<DubboAppServiceGrpc.IAppService> dubboAppService() {
            return new ReferenceBean<>();
        }

        @Bean
        public AuthorizationClient projectAuthorizationClient(DubboAppServiceGrpc.IAppService appService, AuthorizationProperties properties) {
            return new DubboProjectAuthorizationClient(appService, properties.getAppKey(), properties.getAppSecret());
        }
    }

    /**
     * 租户级授权
     */
    @ConditionalOnProperty(prefix = "airiot.client.authorization", name = "type", havingValue = "tenant")
    @Configuration
    public static class TenantAuthorizationConfiguration {
        /**
         * 扩展应用服务
         */
        @Bean
        @DubboReference(
                check = false,
                providedBy = "spm",
                protocol = "grpc",
                retries = 3,
                timeout = 3000,
                scope = org.apache.dubbo.rpc.Constants.SCOPE_REMOTE,
                loadbalance = LoadbalanceRules.ROUND_ROBIN
        )
        public ReferenceBean<cn.airiot.sdk.client.dubbo.grpc.spm.DubboUserServiceGrpc.IUserService> dubboSpmUserService() {
            return new ReferenceBean<>();
        }

        @Bean
        public AuthorizationClient tenantAuthorizationClient(cn.airiot.sdk.client.dubbo.grpc.spm.DubboUserServiceGrpc.IUserService userService,
                                                             AuthorizationProperties properties) {
            return new DubboTenantAuthorizationClient(userService, properties.getAppKey(), properties.getAppSecret());
        }
    }

    /**
     * 核心服务
     */
    @Configuration
    @ComponentScan("cn.airiot.sdk.client.dubbo.clients.core")
    @ConditionalOnServiceEnabled(ServiceType.CORE)
    public static class DubboCoreClientConfiguration {

        private final ServiceConfig serviceConfig;

        public DubboCoreClientConfiguration(ClientProperties properties) {
            this.serviceConfig = properties.getServices().getOrDefault(ServiceType.CORE, properties.getDefaultConfig());
        }

        /**
         * 用户服务
         */
        @Bean
        public ReferenceBean<DubboUserServiceGrpc.IUserService> dubboUserService() {
            return DubboClientUtils.createDubboReference(ServiceType.CORE, this.serviceConfig, DubboUserServiceGrpc.IUserService.class);
        }

        /**
         * 部门服务
         */
        @Bean
        public ReferenceBean<DubboDeptServiceGrpc.IDeptService> dubboDepartmentService() {
            return DubboClientUtils.createDubboReference(ServiceType.CORE, this.serviceConfig, DubboDeptServiceGrpc.IDeptService.class);
        }

        /**
         * 角色服务
         */
        @Bean
        public ReferenceBean<DubboRoleServiceGrpc.IRoleService> dubboRoleService() {
            return DubboClientUtils.createDubboReference(ServiceType.CORE, this.serviceConfig, DubboRoleServiceGrpc.IRoleService.class);
        }

        /**
         * 工作表定义服务
         */
        @Bean
        public ReferenceBean<DubboTableSchemaServiceGrpc.ITableSchemaService> dubboTableSchemaService() {
            return DubboClientUtils.createDubboReference(ServiceType.CORE, this.serviceConfig, DubboTableSchemaServiceGrpc.ITableSchemaService.class);
        }

        /**
         * 工作表记录服务
         */
        @Bean
        public ReferenceBean<DubboTableDataServiceGrpc.ITableDataService> dubboTableDataService() {
            return DubboClientUtils.createDubboReference(ServiceType.CORE, this.serviceConfig, DubboTableDataServiceGrpc.ITableDataService.class);
        }

        /**
         * 系统变量(数据字典)服务
         */
        @Bean
        public ReferenceBean<DubboSystemVariableServiceGrpc.ISystemVariableService> dubboSystemVariableService() {
            return DubboClientUtils.createDubboReference(ServiceType.CORE, this.serviceConfig, DubboSystemVariableServiceGrpc.ISystemVariableService.class);
        }
    }

    /**
     * 数据接口服务
     */
    @Configuration
    @ConditionalOnServiceEnabled(ServiceType.DataService)
    @ComponentScan(basePackages = "cn.airiot.sdk.client.dubbo.clients.ds")
    public static class DubboDataServiceClientConfiguration {

        private final ServiceConfig serviceConfig;

        public DubboDataServiceClientConfiguration(ClientProperties properties) {
            this.serviceConfig = properties.getServices().getOrDefault(ServiceType.DataService, properties.getDefaultConfig());
        }

        /**
         * 数据接口服务客户端
         */
        @Bean
        public ReferenceBean<DubboDataServiceGrpc.IDataService> dubboDataService() {
            return DubboClientUtils.createDubboReference(ServiceType.DataService, this.serviceConfig, DubboDataServiceGrpc.IDataService.class);
        }
    }

    /**
     * 空间管理客户端配置类
     */
    @Configuration
    @ConditionalOnServiceEnabled(ServiceType.SPM)
    @ComponentScan(basePackages = "cn.airiot.sdk.client.dubbo.clients.spm")
    public static class DubboSpmClientConfiguration {

        private final ServiceConfig serviceConfig;

        public DubboSpmClientConfiguration(ClientProperties properties) {
            this.serviceConfig = properties.getServices().getOrDefault(ServiceType.SPM, properties.getDefaultConfig());
        }

        /**
         * 项目信息服务
         */
        @Bean
        public ReferenceBean<DubboProjectServiceGrpc.IProjectService> dubboProjectService() {
            return DubboClientUtils.createDubboReference(ServiceType.SPM, this.serviceConfig, DubboProjectServiceGrpc.IProjectService.class);
        }
    }
}
