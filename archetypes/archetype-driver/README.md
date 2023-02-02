# 驱动开发项目骨架使用说明

## 使用骨架项目创建新项目

```bash
mvn archetype:generate \
    -DgroupId=com.airiot.demo \
    -DartifactId=driver \
    -Dversion=1.0.0 \
    -Dpackage=com.airiot.demo.driver \
    -DarchetypeArtifactId=sdk-archetype-driver \
    -DarchetypeGroupId=cn.airiot \
    -DarchetypeVersion=4.1.0
```
:::tip
在 `windows` 平台使用以下命令

```bash
mvn archetype:generate -DspringBootVersion=2.7.7 -DsdkVersion=4.1.0 -DgroupId=com.airiot.demo -DartifactId=driver -Dversion=1.0.0 -Dpackage=com.airiot.demo.driver -DarchetypeArtifactId=sdk-archetype-driver -DarchetypeGroupId=cn.airiot -DarchetypeVersion=4.1.0
```
:::