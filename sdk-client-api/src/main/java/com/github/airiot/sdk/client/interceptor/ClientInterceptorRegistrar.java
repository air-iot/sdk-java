package com.github.airiot.sdk.client.interceptor;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;


/**
 * 注册相关拦截器定义
 */
public class ClientInterceptorRegistrar implements ImportBeanDefinitionRegistrar {
    
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        BeanDefinition projectBeanDfn = BeanDefinitionBuilder.genericBeanDefinition(ProjectInterceptor.class).getBeanDefinition();
        BeanDefinition authBeanDfn = BeanDefinitionBuilder.genericBeanDefinition(AuthInterceptor.class).getBeanDefinition();
        registry.registerBeanDefinition("projectInterceptor", projectBeanDfn);
        registry.registerBeanDefinition("authInterceptor", authBeanDfn);
    }
}
