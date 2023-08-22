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

package io.github.airiot.sdk.algorithm;


import io.github.airiot.sdk.algorithm.annotation.AlgorithmFunction;

import java.util.Map;

/**
 * 算法应用异步接口定义. 该接口定义了算法应用的生命周期方法, 以及算法函数的执行方法. <br>
 * <b>注: 该接口的实现必须注入到 Spring IoC 容器中, 否则 SDK 无法识别.</b>
 * <br>
 * <p>
 * 可以在该接口的实现类中, 使用 {@link AlgorithmFunction} 注解定义算法函数.
 * 也可以不使用该注解, 而是在 {@link #run(String, String, Map)} 方法中根据 {@code function} 参数的值, 执行对应的算法函数.
 * <br>
 * <br>
 * 示例代码如下:
 * <pre>{@code
 *  @Component
 *  public class MyAlgorithmApp implements AlgorithmApp {
 *
 *      @Override
 *      public void start() {
 *          // 算法服务启动时, 执行一些初始化操作
 *      }
 *
 *      @Override
 *      public void stop() {
 *          // 算法服务停止时, 执行一些清理操作
 *      }
 *
 *      @AlgorithmFunction("algorithm1")
 *      public Map<String, Object> algorithm1(String projectId, Map<String, Object> params) {
 *          // 自定义算法函数1
 *      }
 *
 *      @AlgorithmFunction("algorithm2")
 *      public CustomResult algorithm2(String projectId, CustomParam params) {
 *          // 自定义算法函数2
 *      }
 *
 *      @Override
 *      public Object run(String projectId, String function, Map<String, Object> params) {
 *          // 其它未定义的算法函数, 会调用此方法, 可以在该方法内根据 function 的值执行对应的算法逻辑
 *      }
 *  }
 * }</pre>
 */
public interface AlgorithmApp {

    /**
     * 当算法服务启动时，会调用此方法
     */
    default void start() {

    }

    /**
     * 当算法服务停止时，会调用此方法
     */
    default void stop() {

    }

    /**
     * 获取算法的 schema 定义信息
     */
    String schema();

    /**
     * 执行算法. 如果在当前类型中没有找到使用 {@link AlgorithmFunction} 定义方法, 则会调用此方法
     *
     * @param projectId 发起请求的项目ID
     * @param function  函数名
     * @param params    请求参数
     * @return 算法执行结果
     * @throws AlgorithmException 算法执行异常, 该异常消息会作为错误信息返回给调用方
     */
    default Object run(String projectId, String function, Map<String, Object> params) throws AlgorithmException {
        throw new IllegalArgumentException("未实现的算法 '" + function + "'");
    }
}
