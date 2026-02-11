package io.github.airiot.sdk.client.http;

import com.google.gson.reflect.TypeToken;
import io.github.airiot.sdk.client.dto.ResponseDTO;
import io.github.airiot.sdk.client.gson.CustomGson;
import io.github.airiot.sdk.client.service.Constants;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.reactivestreams.Publisher;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.codec.HttpMessageDecoder;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CustomHttpMessageDecoder implements HttpMessageDecoder<ResponseDTO<?>> {

    private final int maxInMemorySize;

    public CustomHttpMessageDecoder() {
        this(256 * 1024);
    }

    public CustomHttpMessageDecoder(int maxInMemorySize) {
        this.maxInMemorySize = maxInMemorySize;
    }

    @Override
    public boolean canDecode(@NonNull ResolvableType elementType, @Nullable MimeType mimeType) {
        return MimeTypeUtils.APPLICATION_JSON.isCompatibleWith(mimeType) && elementType.getRawClass() != null && ResponseDTO.class.isAssignableFrom(elementType.getRawClass());
    }

    @Override
    public @NonNull Flux<ResponseDTO<?>> decode(@NonNull Publisher<DataBuffer> input,
                                                @NonNull ResolvableType elementType,
                                                @Nullable MimeType mimeType,
                                                @Nullable Map<String, Object> hints) {
        HttpStatusCode statusCode = (HttpStatusCode) hints.get("statusCode");
        int count = (int) hints.get("count");
        // 获取 ResponseDTO 中的泛型
        Type clazz = elementType.getGeneric(0).getType();
        return DataBufferUtils.join(input, this.maxInMemorySize)
                .flatMapMany(dataBuffer -> {
                    Charset charset = mimeType == null ? StandardCharsets.UTF_8 : mimeType.getCharset();
                    if (charset == null) {
                        charset = StandardCharsets.UTF_8;
                    }
                    String body = dataBuffer.toString(charset);
                    return Mono.just(this.decodeResponse(statusCode, count, body, clazz));
                });
    }

    @Override
    public @NonNull Mono<ResponseDTO<?>> decodeToMono(@NonNull Publisher<DataBuffer> input,
                                                      @NonNull ResolvableType elementType,
                                                      @Nullable MimeType mimeType,
                                                      @Nullable Map<String, Object> hints) {
        HttpStatusCode statusCode = (HttpStatusCode) hints.get("statusCode");
        int count = (int) hints.get("count");

        // 获取 ResponseDTO 中的泛型
        Type clazz = elementType.getGeneric(0).getType();
        return DataBufferUtils.join(input, this.maxInMemorySize)
                .flatMap(dataBuffer -> {
                    Charset charset = mimeType == null ? StandardCharsets.UTF_8 : mimeType.getCharset();
                    if (charset == null) {
                        charset = StandardCharsets.UTF_8;
                    }
                    String body = dataBuffer.toString(charset);
                    return Mono.just(this.decodeResponse(statusCode, count, body, clazz));
                });

    }

    @Override
    public @NonNull List<MimeType> getDecodableMimeTypes() {
        return List.of(MimeTypeUtils.APPLICATION_JSON);
    }

    ResponseDTO<?> decodeResponse(HttpStatusCode statusCode, int count, String body, Type clazz) {
        int code = statusCode.value();
        if (statusCode.is2xxSuccessful()) {
            if (clazz == Void.class) {
                return new ResponseDTO<>(true, 0, 200, "OK", "", null);
            }

            if (!StringUtils.hasText(body)) {
                return new ResponseDTO<>(true, 0, code, "OK", "", null);
            }

            // 如果返回值是 String 类型, 则直接返回字符串
            if (clazz == String.class) {
                return new ResponseDTO<>(true, count, code, "OK", "", body);
            }

            Object result = CustomGson.GSON.fromJson(body, clazz);
            return new ResponseDTO<>(true, count, code, "OK", "", result);
        }

        if (!StringUtils.hasText(body)) {
            return new ResponseDTO<>(false, 0, code, "未知原因", "响应体为空", null);
        }

        ResponseDTO<?> error = CustomGson.GSON.fromJson(body, TypeToken.getParameterized(ResponseDTO.class, Void.class).getType());
        if (error == null) {
            return new ResponseDTO<>(false, 0, code, "未知原因", body, null);
        }

        return new ResponseDTO<>(false, 0, code, error.getMessage(), error.getDetail(), error.getField(), null);
    }

    @Override
    public @NonNull Map<String, Object> getDecodeHints(@NonNull ResolvableType actualType,
                                                       @NonNull ResolvableType elementType,
                                                       @NonNull ServerHttpRequest request,
                                                       ServerHttpResponse response) {
        HttpHeaders headers = response.getHeaders();
        int count = 0;
        String headerCount = headers.getFirst(Constants.HEADER_COUNT);
        if (StringUtils.hasText(headerCount)) {
            count = Integer.parseInt(headerCount);
        }
        return Map.of(
                "statusCode", Objects.requireNonNull(response.getStatusCode()),
                "count", count
        );
    }
}
