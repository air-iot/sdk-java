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

package io.github.airiot.sdk.client.dubbo.clients.spm;


import io.github.airiot.sdk.client.builder.Query;
import io.github.airiot.sdk.client.dto.ResponseDTO;
import io.github.airiot.sdk.client.service.spm.dto.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("integration")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProjectClientTests {

    @Autowired
    private DubboProjectClient projectClient;

    private final String projectId = "integration-project";

    @Test
    @Order(1)
    void createProject() {
        LicenseContent grant = new LicenseContent();
        grant.setUserCount(1);
        grant.setStartTime("2023-02-21");
        grant.setValidityPeriod(31);

        Project project = new Project("集成测试项目-可删除", grant);
        project.setId("integration-project");
        ResponseDTO<ProjectCreateResult> responseDTO = this.projectClient.create(project);
        Assertions.assertTrue(responseDTO.isSuccess(), responseDTO.getFullMessage());
    }

    @Test
    @Order(2)
    void queryAll() {
        ResponseDTO<List<Project>> responseDTO = this.projectClient.queryAll();
        Assertions.assertTrue(responseDTO.isSuccess(), responseDTO.getFullMessage());
    }

    @Test
    @Order(3)
    void queryByName() {
        ResponseDTO<List<Project>> responseDTO = this.projectClient.query(Query.newBuilder()
                .filter()
                .eq(Project::getName, "集成测试项目-可删除")
                .end()
                .build());

        Assertions.assertTrue(responseDTO.isSuccess(), responseDTO.getFullMessage());
        Assertions.assertNotNull(responseDTO.getData(), "未查询到项目");
        Assertions.assertFalse(responseDTO.getData().isEmpty(), "未查询到项目");
    }

    @Test
    @Order(4)
    void queryById() {
        ResponseDTO<Project> responseDTO = this.projectClient.queryById(this.projectId);
        Assertions.assertTrue(responseDTO.isSuccess(), responseDTO.getFullMessage());
        Assertions.assertNotNull(responseDTO.getData(), "未查询到项目信息: " + this.projectId);
    }

    @Test
    @Order(5)
    void updateProjectBasicInfo() {
        Project project = new Project();
        project.setId("63f73b4f2da4c51d89994289");
        project.setName("集成测试项目-可删除-update");
        project.setBgColor("rgba(126,201,214,1)");
        project.setIndustry("智慧城市");
        project.setProjectType("创建空白项目");
        project.setRemarks("这是一条新备注");

        ResponseDTO<Void> responseDTO = this.projectClient.update(project);
        Assertions.assertTrue(responseDTO.isSuccess(), responseDTO.getFullMessage());
    }

    @Test
    @Order(6)
    void updateProjectLicense() {
        LicenseContent license = new LicenseContent();
        license.setStartTime("2023-02-23");
        license.setValidityPeriod(90);
        license.setUserCount(3);

        LicenseFlow flow = new LicenseFlow();
        flow.setFlowCount(5);
        flow.setFlowExecCount(1000);
        flow.setTableCount(20);

        license.setFlow(flow);

        ResponseDTO<Void> responseDTO = this.projectClient.updateLicense(this.projectId, license);
        Assertions.assertTrue(responseDTO.isSuccess(), responseDTO.getFullMessage());
    }

    @Test
    @Order(7)
    void replaceProject() {
        Project project = new Project();
        project.setId(this.projectId);
        project.setName("集成测试项目-可删除-replace");
        project.setStatus(false);
        project.setBgColor("rgba(126,201,214,1)");
        project.setIndustry("智慧城市");
        project.setProjectType("创建空白项目");
        project.setRemarks("这是一条新备注");

        LicenseContent grant = new LicenseContent();
        grant.setStartTime("2023-02-27");
        grant.setValidityPeriod(45);
        grant.setUserCount(3);

        LicenseFlow flow = new LicenseFlow();
        flow.setFlowCount(5);
        flow.setFlowExecCount(1000);
        flow.setTableCount(20);
        grant.setFlow(flow);

        LicenseVisual visual = new LicenseVisual();
        visual.setGis(true);
        visual.setThreeD(false);
        visual.setVideo(true);
        visual.setDashboardCount(20);
        visual.setVideoCount(120);
        grant.setVisual(visual);

        project.setGrant(grant);

        ResponseDTO<Void> responseDTO = this.projectClient.replace(project);
        Assertions.assertTrue(responseDTO.isSuccess(), responseDTO.getFullMessage());
    }

    @Test
    @Order(8)
    void deleteById() {
        ResponseDTO<Void> responseDTO = this.projectClient.deleteById(this.projectId);
        Assertions.assertTrue(responseDTO.isSuccess(), responseDTO.getFullMessage());
    }
}
