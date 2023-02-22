package cn.airiot.sdk.client.dubbo.configuration;


import cn.airiot.sdk.client.dubbo.clients.DubboProjectAuthorizationClient;
import cn.airiot.sdk.client.dubbo.clients.DubboTenantAuthorizationClient;
import cn.airiot.sdk.client.dubbo.clients.core.*;
import cn.airiot.sdk.client.dubbo.clients.ds.DubboDataServiceClient;
import cn.airiot.sdk.client.dubbo.clients.spm.DubboProjectClient;
import cn.airiot.sdk.client.dubbo.clients.spm.DubboSpmUserClient;
import cn.airiot.sdk.client.dubbo.clients.warning.DubboRuleClient;
import cn.airiot.sdk.client.dubbo.clients.warning.DubboWarnClient;
import cn.airiot.sdk.client.dubbo.grpc.core.*;
import cn.airiot.sdk.client.dubbo.grpc.datasource.DubboDataServiceGrpc;
import cn.airiot.sdk.client.dubbo.grpc.spm.DubboProjectServiceGrpc;
import cn.airiot.sdk.client.dubbo.grpc.warning.DubboRuleServiceGrpc;
import cn.airiot.sdk.client.dubbo.grpc.warning.DubboWarnServiceGrpc;
import cn.airiot.sdk.client.dubbo.utils.DubboClientUtils;
import cn.airiot.sdk.client.interceptor.EnableClientInterceptors;
import cn.airiot.sdk.client.properties.AuthorizationProperties;
import cn.airiot.sdk.client.properties.ClientProperties;
import cn.airiot.sdk.client.properties.ServiceConfig;
import cn.airiot.sdk.client.properties.ServiceType;
import cn.airiot.sdk.client.properties.condition.ConditionalOnServiceEnabled;
import cn.airiot.sdk.client.service.AuthorizationClient;
import cn.airiot.sdk.client.service.core.AppClient;
import cn.airiot.sdk.client.service.spm.SpmUserClient;
import org.apache.dubbo.common.constants.LoadbalanceRules;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.spring.ReferenceBean;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
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
        public AppClient dubboAppClient(DubboAppServiceGrpc.IAppService appService) {
            return new DubboAppClient(appService);
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
        public SpmUserClient spmUserClient(cn.airiot.sdk.client.dubbo.grpc.spm.DubboUserServiceGrpc.IUserService userService) {
            return new DubboSpmUserClient(userService);
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
//    @ComponentScan("cn.airiot.sdk.client.dubbo.clients.core")
    @ConditionalOnServiceEnabled(ServiceType.CORE)
    public static class DubboCoreClientConfiguration {

        private final ServiceConfig serviceConfig;

        public DubboCoreClientConfiguration(ClientProperties properties) {
            this.serviceConfig = properties.getServices().getOrDefault(ServiceType.CORE, properties.getDefaultConfig());
        }

        @Bean
        public DubboDepartmentClient dubboDepartmentClient(DubboDeptServiceGrpc.IDeptService deptService) {
            return new DubboDepartmentClient(deptService);
        }

        @Bean
        public DubboUserClient dubboUserClient(DubboUserServiceGrpc.IUserService userService) {
            return new DubboUserClient(userService);
        }

        @Bean
        public DubboRoleClient dubboRoleClient(DubboRoleServiceGrpc.IRoleService roleService) {
            return new DubboRoleClient(roleService);
        }

        @Bean
        public DubboTableSchemaClient dubboTableSchemaClient(DubboTableSchemaServiceGrpc.ITableSchemaService tableSchemaService) {
            return new DubboTableSchemaClient(tableSchemaService);
        }

        @Bean
        public DubboTableDataClient dubboTableDataClient(DubboTableDataServiceGrpc.ITableDataService dataService) {
            return new DubboTableDataClient(dataService);
        }

        @Bean
        public DubboSystemVariableClient dubboSystemVariableClient(DubboSystemVariableServiceGrpc.ISystemVariableService systemVariableService) {
            return new DubboSystemVariableClient(systemVariableService);
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
    @ConditionalOnServiceEnabled(ServiceType.DATA_SERVICE)
//    @ComponentScan(basePackages = "cn.airiot.sdk.client.dubbo.clients.ds")
    public static class DubboDataServiceClientConfiguration {

        private final ServiceConfig serviceConfig;

        public DubboDataServiceClientConfiguration(ClientProperties properties) {
            this.serviceConfig = properties.getServices().getOrDefault(ServiceType.DATA_SERVICE, properties.getDefaultConfig());
        }

        @Bean
        public DubboDataServiceClient dubboDataServiceClient(DubboDataServiceGrpc.IDataService dataService) {
            return new DubboDataServiceClient(dataService);
        }

        /**
         * 数据接口服务客户端
         */
        @Bean
        public ReferenceBean<DubboDataServiceGrpc.IDataService> dubboDataService() {
            return DubboClientUtils.createDubboReference(ServiceType.DATA_SERVICE, this.serviceConfig, DubboDataServiceGrpc.IDataService.class);
        }
    }

    /**
     * 告警客户端配置类
     */
    @Configuration
    @ConditionalOnServiceEnabled(ServiceType.WARNING)
    // @ComponentScan(basePackages = "cn.airiot.sdk.client.dubbo.clients.warning")
    public static class DubboWarningClientConfiguration {

        private final ServiceConfig serviceConfig;

        public DubboWarningClientConfiguration(ClientProperties properties) {
            this.serviceConfig = properties.getServices().getOrDefault(ServiceType.WARNING, properties.getDefaultConfig());
        }

        @Bean
        public DubboRuleClient dubboRuleClient(DubboRuleServiceGrpc.IRuleService ruleService) {
            return new DubboRuleClient(ruleService);
        }

        @Bean
        public DubboWarnClient dubboWarnClient(DubboWarnServiceGrpc.IWarnService warnService) {
            return new DubboWarnClient(warnService);
        }

        /**
         * 告警信息客户端
         */
        @Bean
        public ReferenceBean<DubboWarnServiceGrpc.IWarnService> dubboWarningService() {
            return DubboClientUtils.createDubboReference(ServiceType.WARNING, this.serviceConfig, DubboWarnServiceGrpc.IWarnService.class);
        }

        /**
         * 告警规则客户端
         */
        @Bean
        public ReferenceBean<DubboRuleServiceGrpc.IRuleService> dubboWarningRuleService() {
            return DubboClientUtils.createDubboReference(ServiceType.WARNING, this.serviceConfig, DubboRuleServiceGrpc.IRuleService.class);
        }
    }

    /**
     * 空间管理客户端配置类
     */
    @Configuration
    @ConditionalOnServiceEnabled(ServiceType.SPM)
//    @ComponentScan(basePackages = "cn.airiot.sdk.client.dubbo.clients.spm")
    public static class DubboSpmClientConfiguration {

        private final ServiceConfig serviceConfig;

        public DubboSpmClientConfiguration(ClientProperties properties) {
            this.serviceConfig = properties.getServices().getOrDefault(ServiceType.SPM, properties.getDefaultConfig());
        }

        @Bean
        public DubboProjectClient dubboProjectClient(DubboProjectServiceGrpc.IProjectService projectService) {
            return new DubboProjectClient(projectService);
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
