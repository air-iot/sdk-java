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

package io.github.airiot.sdk.algorithm.annotation;

import io.github.airiot.sdk.algorithm.AlgorithmApp;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

/**
 * 算法函数注解. 该注解只能添加到方法上.
 * <br>
 * 程序启动时, SDK 会扫描实现 {@link AlgorithmApp} 接口的类中的所有方法, 并将添加了该注解的方法注册为算法函数.
 * <br>
 * 当接收到平台的算法执行请求时, SDK 会根据请求中的函数名, 在已注册的算法函数中查找对应的函数, 并执行该函数.
 * 如果没有找到对应的函数, 则会调用 {@link AlgorithmApp#run(String, String, Map)} 方法.
 * <br><br>
 * 对于使用该注解标注的方法, 有以下要求: <br>
 * 1. 方法必须是 public 的, 且不能是 static 的方法. <br>
 * 2. 方法的返回值类型必须是自定义类型或 {@code Map<String,Object>}, 且返回值包含的数据与 schema 中定义的字段和数据类型一致. <br>
 * 3. 方法要求接收 1 或 2 个参数. 第 1 个参数必须是 String 类型, 该参数用于接收 projectId 信息.
 * 第 2 个参数(如果该函数有输入参数)必须为 {@code Map<String, Object>} 类型或自定义类型, 该参数用于接收请求参数. <br>
 *
 * <br>
 * 示例代码如下:
 *
 * <pre>{@code
 * // 自定义请求参数类型
 * public class Algorithm2Params {
 *
 *     private Integer param1;
 *     private String param2;
 *
 *     public Integer getParam1() {
 *         return param1;
 *     }
 *
 *     public String getParam2() {
 *         return param2;
 *     }
 * }
 *
 * public class Algorithm2Result {
 *     private Integer total;
 *
 *     public Integer getTotal() {
 *         return total;
 *     }
 *
 *     public void setTotal(Integer total) {
 *         this.total = total;
 *     }
 *
 * }
 *
 * // 自定义算法服务实现类
 * public class MyAlgorithm implements AlgorithmApp {
 *
 *     // 该方法同时接收 projectId 和 Map<String, Object> 请求参数
 *     @AlgorithmFunction("algorithm1")
 *     public Map<String, Object> algorithm1(String projectId, Map<String, Object> params) {
 *          // 执行算法逻辑
 *     }
 *
 *     // 该方法同时接收 projectId 和自定义类型的请求参数, 并且返回值为自定义类型
 *     @AlgorithmFunction("algorithm2")
 *     public Algorithm2Result algorithm2(String projectId, Algorithm2Params params) {
 *          // 执行算法逻辑
 *     }
 *
 *     // 该方法只接收 projectId 参数
 *     @AlgorithmFunction("algorithm3")
 *     public Map<String, Object> algorithm3(String projectId) {
 *         // 执行算法逻辑
 *     }
 *
 *     // 当未找到注册的算法函数时, 会调用该方法.
 *     @Override
 *     public Object run(String projectId, String functionName, Map<String, Object> params) {
 *          // 该方法用于处理未注册的算法函数请求
 *          if("algorithm4".equals(functionName)) {
 *              // 执行算法4逻辑
 *          }
 *     }
 * }}
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AlgorithmFunction {

    /**
     * 函数名
     */
    String value();
}
