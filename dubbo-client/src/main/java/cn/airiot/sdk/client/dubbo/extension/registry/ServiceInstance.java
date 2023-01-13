package cn.airiot.sdk.client.dubbo.extension.registry;

import java.util.List;
import java.util.Map;


/**
 * 服务实例信息
 */
public class ServiceInstance {

    /**
     * 服务实例ID
     */
    private String id;
    /**
     * 服务名称
     */
    private String name;
    /**
     * 服务实例版本号
     */
    private String version;
    /**
     * 元数据
     */
    private Map<String, String> metadata;
    /**
     * 服务实例 Endpoints
     */
    private List<String> endpoints;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setEndpoints(List<String> endpoints) {
        this.endpoints = endpoints;
    }

    public List<String> getEndpoints() {
        return endpoints;
    }

    @Override
    public String toString() {
        return "ServiceInstance{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", metadata=" + metadata +
                ", endpoints=" + endpoints +
                '}';
    }
}
