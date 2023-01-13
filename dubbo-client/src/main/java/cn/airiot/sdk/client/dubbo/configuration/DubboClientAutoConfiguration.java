package cn.airiot.sdk.client.dubbo.configuration;


import cn.airiot.sdk.client.dubbo.configuration.properties.DubboClientProperties;
import cn.airiot.sdk.client.dubbo.grpc.core.DubboTableDataServiceGrpc;
import cn.airiot.sdk.client.dubbo.grpc.core.DubboUserServiceGrpc;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.spring.ReferenceBean;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Dubbo 客户端配置类
 */
@EnableDubbo(scanBasePackages = {"cn.airiot.sdk.client.dubbo.clients"})
@EnableConfigurationProperties({DubboClientProperties.class})
@ComponentScan("cn.airiot.sdk.client.dubbo")
@Configuration
public class DubboClientAutoConfiguration {

    /**
     * 用户服务
     */
    @Bean
    @DubboReference(
            check = false,
            providedBy = "core",
            protocol = "grpc",
            scope = org.apache.dubbo.rpc.Constants.SCOPE_REMOTE
    )
    public ReferenceBean<DubboUserServiceGrpc.IUserService> dubboUserService() {
        return new ReferenceBean<>();
    }
    
    /**
     * 工作表记录服务
     */
    @Bean
    @DubboReference(
            check = false,
            providedBy = "core",
            protocol = "grpc",
            scope = org.apache.dubbo.rpc.Constants.SCOPE_REMOTE
    )
    public ReferenceBean<DubboTableDataServiceGrpc.ITableDataService> dubboTableDataService() {
        return new ReferenceBean<>();
    }
}
