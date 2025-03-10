package io.github.airiot.sdk.client.http.clients.core;

import io.github.airiot.sdk.client.dto.InsertResult;
import io.github.airiot.sdk.client.dto.ResponseDTO;
import io.github.airiot.sdk.client.service.core.TableRecordClient;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("integration")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HttpTableRecordClientTests {

    @Autowired
    private TableRecordClient tableRecordClient;

    @Test
    @Order(1)
    void queryById() {
        ResponseDTO<Map<String, Object>> response = tableRecordClient.get("6757fd2d3fb6d4a5fe0beb1b");
        Assertions.assertTrue(response.isSuccess(), response.getMessage());
        Assertions.assertNotNull(response.getData(), "未查询工作表信息");
        System.out.println(response.getData());
    }

    @Test
    @Order(2)
    void update() {
        // "gis": {
        //			"location": {
        //				"center": [
        //					12901806.298425384,
        //					4892188.191220543
        //				],
        //				"data": [{
        //					"coordinates": {
        //						"center": [
        //							116.3237028172677,
        //							40.01868794331614
        //						],
        //						"radius": 57102.09256302193
        //					},
        //					"type": "Circle"
        //				}],
        //				"lonlatType": "normal",
        //				"tile": "高德地图",
        //				"view": {
        //					"animations_": [],
        //					"constraints_": {},
        //					"dispatching_": {},
        //					"disposed": false,
        //					"hints_": [
        //						0,
        //						0
        //					],
        //					"listeners_": {},
        //					"maxResolution_": 19567.87924100512,
        //					"minResolution_": 0.5971642834779395,
        //					"minZoom_": 3,
        //					"nextCenter_": null,
        //					"nextResolution_": null,
        //					"nextRotation_": null,
        //					"ol_uid": "2",
        //					"pendingRemovals_": {},
        //					"projection_": {
        //						"axisOrientation_": "enu",
        //						"canWrapX_": true,
        //						"code_": "EPSG:3857",
        //						"defaultTileGrid_": null,
        //						"extent_": [-20037508.342789244, -20037508.342789244,
        //							20037508.342789244,
        //							20037508.342789244
        //						],
        //						"global_": true,
        //						"units_": "m",
        //						"worldExtent_": [-180, -85,
        //							180,
        //							85
        //						]
        //					},
        //					"revision_": 4,
        //					"targetCenter_": [
        //						12949095.364806324,
        //						4868658.332233886
        //					],
        //					"targetResolution_": 611.49622628141,
        //					"targetRotation_": 0,
        //					"values_": {
        //						"center": [
        //							12949095.364806324,
        //							4868658.332233886
        //						],
        //						"maxZoom": 18,
        //						"minZoom": 3,
        //						"projection": {
        //							"axisOrientation_": "enu",
        //							"canWrapX_": true,
        //							"code_": "EPSG:3857",
        //							"defaultTileGrid_": null,
        //							"extent_": [-20037508.342789244, -20037508.342789244,
        //								20037508.342789244,
        //								20037508.342789244
        //							],
        //							"global_": true,
        //							"units_": "m",
        //							"worldExtent_": [-180, -85,
        //								180,
        //								85
        //							]
        //						},
        //						"resolution": 611.49622628141,
        //						"rotation": 0,
        //						"zoom": 8
        //					},
        //					"viewportSize_": [
        //						942,
        //						800
        //					],
        //					"zoomFactor_": 2
        //				},
        //				"zoom": 8
        //			},
        //			"lonlatType": "normal"
        //		}


        Map<String, Object> gisData = new HashMap<>();
        Map<String, Object> location = new HashMap<>();
        location.put("center", Arrays.asList(12901806.298425384, 4892188.191220543));
        location.put("lonlatType", "normal");
        location.put("tile", "高德地图");
        location.put("zoom", 8);

        Map<String, Object> coordinates = new HashMap<>();
        coordinates.put("center", Arrays.asList(116.3237028172677, 40.01868794331614));
        coordinates.put("radius", 57102.09256302193);

        Map<String, Object> circleData = new HashMap<>();
        circleData.put("coordinates", coordinates);
        circleData.put("type", "Circle");

        location.put("data", Collections.singletonList(circleData));

        Map<String, Object> view = new HashMap<>();
        view.put("zoom", 1);
        view.put("resolution", 611.49622628141);
        view.put("rotation", 0);
        location.put("view", view);

        gisData.put("location", location);
        gisData.put("lonlatType", "normal");

        Map<String, Object> gisRecord = new HashMap<>();
        gisRecord.put("gis", gisData);

        ResponseDTO<Void> response = tableRecordClient.update("6757fd2d3fb6d4a5fe0beb1b", gisRecord);
        Assertions.assertTrue(response.isSuccess(), response.getMessage());
        Assertions.assertNotNull(response.getData(), "未查询工作表信息");
        System.out.println(response.getData());
    }

    @Test
    @Order(3)
    void create() {
        Map<String, Object> gisData = new HashMap<>();
        Map<String, Object> location = new HashMap<>();
        location.put("center", Arrays.asList(12901806.298425384, 4892188.191220543));
        location.put("lonlatType", "normal");
        location.put("tile", "高德地图");
        location.put("zoom", 8);

        Map<String, Object> coordinates = new HashMap<>();
        coordinates.put("center", Arrays.asList(116.3237028172677, 40.01868794331614));
        coordinates.put("radius", 57102.09256302193);

        Map<String, Object> circleData = new HashMap<>();
        circleData.put("coordinates", coordinates);
        circleData.put("type", "Circle");

        location.put("data", Collections.singletonList(circleData));

        Map<String, Object> view = new HashMap<>();
        view.put("zoom", 1);
        view.put("resolution", 611.49622628141);
        view.put("rotation", 0);
        location.put("view", view);

        gisData.put("location", location);
        gisData.put("lonlatType", "normal");

        Map<String, Object> gisRecord = new HashMap<>();
        gisRecord.put("gis", gisData);

        // 6776147f6d81617b465c3122
        ResponseDTO<InsertResult> response = tableRecordClient.create("opcda_win_dll", "opcda_dll_003", gisRecord);
        Assertions.assertTrue(response.isSuccess(), response.getMessage());
        Assertions.assertNotNull(response.getData(), "未查询工作表信息");
        System.out.println(response.getData());
    }

    @Test
    @Order(4)
    void replace() {
        Map<String, Object> gisData = new HashMap<>();
        Map<String, Object> location = new HashMap<>();
        location.put("center", Arrays.asList(12901806.298425384, 4892188.191220543));
        location.put("lonlatType", "normal");
        location.put("tile", "高德地图");
        location.put("zoom", 8);

        Map<String, Object> coordinates = new HashMap<>();
        coordinates.put("center", Arrays.asList(116.3237028172677, 40.01868794331614));
        coordinates.put("radius", 57102.09256302193);

        Map<String, Object> circleData = new HashMap<>();
        circleData.put("coordinates", coordinates);
        circleData.put("type", "Circle");

        location.put("data", Collections.singletonList(circleData));

        Map<String, Object> view = new HashMap<>();
        view.put("zoom", 8);
        view.put("resolution", 611.49622628141);
        view.put("rotation", 0);
        location.put("view", view);

        gisData.put("location", location);
        gisData.put("lonlatType", "normal");

        Map<String, Object> gisRecord = new HashMap<>();
        gisRecord.put("gis", gisData);

        // 6776147f6d81617b465c3122
        ResponseDTO<Void> response = tableRecordClient.replace("6776147f6d81617b465c3122", "opcda_win_dll", "opcda_dll_003", gisRecord);
        Assertions.assertTrue(response.isSuccess(), response.getMessage());
        System.out.println(response.getData());
    }
}
