package cn.airiot.sdk.driver.data.handlers;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TagValueHandlerTests {

    private final TagValueHandler handler = new TagValueHandler();

    @Test
    void testSupports() {
        Assertions.assertFalse(handler.supports("dataPoint-1", null, 123));
    }
}
