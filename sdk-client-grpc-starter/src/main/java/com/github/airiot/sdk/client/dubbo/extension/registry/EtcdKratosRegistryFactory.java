package com.github.airiot.sdk.client.dubbo.extension.registry;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.registry.Registry;
import org.apache.dubbo.registry.RegistryFactory;

public class EtcdKratosRegistryFactory implements RegistryFactory {

    @Override
    public Registry getRegistry(URL url) {
        return new EtcdKratosRegistry(url);
    }
}
