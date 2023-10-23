/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.airiot.sdk.logger.processor;

import com.google.auto.service.AutoService;
import io.github.airiot.sdk.logger.LoggerModules;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.processing.*;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 日志模块处理器. 用于将 {@link LoggerModules} 注解标记的枚举类的值, 生成一个日志模块列表文件
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes("io.github.airiot.sdk.logger.LoggerModules")
public class LoggerModuleProcessor extends AbstractProcessor {

    private Messager messager;
    private Elements elementUtils;

    private final List<File> serviceYamlFiles = new ArrayList<>(2);
    private String serviceYamls;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.messager = processingEnv.getMessager();
        this.elementUtils = processingEnv.getElementUtils();
        this.serviceYamls = processingEnv.getOptions().get("serviceYamls");
        if (serviceYamls == null || serviceYamls.trim().isEmpty()) {
            this.messager.printMessage(Diagnostic.Kind.WARNING, "未找到编译参数 -AserviceYamls, 请检查是否配置了该参数");
        } else {
            this.messager.printMessage(Diagnostic.Kind.NOTE, String.format("编译参数 -AserviceYamls: %s", serviceYamls));

            System.out.println("日志模块输出文件: " + serviceYamls);

            for (String yaml : serviceYamls.split(",")) {
                File file = new File(yaml);
                if (!file.exists()) {
                    throw new IllegalArgumentException("未找到文件 " + yaml);
                }
                this.serviceYamlFiles.add(file);
            }
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        this.messager.printMessage(Diagnostic.Kind.NOTE, "------------------------------------------------------------------------");
        this.messager.printMessage(Diagnostic.Kind.NOTE, "处理日志模块信息");
        this.messager.printMessage(Diagnostic.Kind.NOTE, "------------------------------------------------------------------------");
        List<String> allModules = new ArrayList<>();
        for (TypeElement annotation : annotations) {
            System.out.println("typeElements:" + annotation);
            for (Element element : roundEnv.getElementsAnnotatedWith(annotation)) {
                if (!ElementKind.INTERFACE.equals(element.getKind())) {
                    this.messager.printMessage(Diagnostic.Kind.WARNING, String.format("%s 不是接口, 忽略", element.getSimpleName()));
                    continue;
                }
                this.messager.printMessage(Diagnostic.Kind.NOTE, String.format("日志模块列表: %s", element));

                List<String> modules = element.accept(new LoggerModulesVisitor(this.elementUtils), null);

                this.messager.printMessage(Diagnostic.Kind.NOTE, String.format("日志模块列表: %s", modules));

                allModules.addAll(modules);
            }
        }

        if (!allModules.isEmpty()) {
            this.process(allModules);
        }

        return false;
    }

    private void process(List<String> modules) {
        this.messager.printMessage(Diagnostic.Kind.NOTE, String.format("全部日志模块定义: %s", modules));
        this.messager.printMessage(Diagnostic.Kind.NOTE, String.format("服务定义文件列表: %s", this.serviceYamls));

        System.out.println("全部日志模块定义: " + modules);
        System.out.println("日志模块输出文件: " + serviceYamls);

        DumperOptions options = new DumperOptions();
        options.setPrettyFlow(true);
        options.setIndent(2);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml dumpYaml = new Yaml(options);
        
        for (File file : this.serviceYamlFiles) {
            if (file.isDirectory()) {
                File[] files = file.listFiles(subFile -> subFile.getName().endsWith(".yml") || subFile.getName().endsWith(".yaml"));
                if (files == null || files.length == 0) {
                    System.out.println("[WARN] 未在目录 '" + file.getAbsolutePath() + "' 中找到 yaml 文件");
                    continue;
                }

                for (File subFile : files) {
                    this.modifyServiceYamlFile(dumpYaml, subFile, modules);
                }
            } else {
                this.modifyServiceYamlFile(dumpYaml, file, modules);
            }
        }
    }

    private void modifyServiceYamlFile(Yaml dumpYaml, File file, List<String> modules) {
        System.out.println("写入日志模块列表到文件 '" + file.getAbsolutePath() + "'");
        Map<String, Object> keyValues = null;
        try (FileInputStream fis = new FileInputStream(file)) {
            keyValues = new Yaml().load(fis);
            keyValues.put("Module", modules);
        } catch (IOException e) {
            throw new IllegalArgumentException("读取日志模块列表到文件 '" + file.getAbsolutePath() + "' 失败", e);
        }

        try (FileWriter writer = new FileWriter(file)) {
            dumpYaml.dump(keyValues, writer);
        } catch (IOException e) {
            throw new IllegalArgumentException("写入日志模块列表到文件 '" + file.getAbsolutePath() + "' 失败", e);
        }
    }
}
