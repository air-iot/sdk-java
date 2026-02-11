package io.github.airiot.sdk.client.http;

import io.github.airiot.sdk.client.dto.ResponseDTO;
import io.github.airiot.sdk.client.service.Constants;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.core.ResolvableType;
import org.springframework.http.*;
import org.springframework.http.client.reactive.ClientHttpResponse;
import org.springframework.http.codec.DecoderHttpMessageReader;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

public class CustomHttpMessageReader extends DecoderHttpMessageReader<ResponseDTO<?>> {

    public CustomHttpMessageReader() {
        super(new CustomHttpMessageDecoder(256 * 1024));
    }

    @Override
    public @NonNull List<MediaType> getReadableMediaTypes() {
        return List.of(MediaType.APPLICATION_JSON);
    }

    @Override
    public boolean canRead(@NonNull ResolvableType elementType, @Nullable MediaType mediaType) {
        return MediaType.APPLICATION_JSON.isCompatibleWith(mediaType) && elementType.getRawClass() != null && ResponseDTO.class.isAssignableFrom(elementType.getRawClass());
    }

    @Override
    protected @NonNull Map<String, Object> getReadHints(@NonNull ResolvableType elementType,
                                                        @NonNull ReactiveHttpInputMessage message) {
        HttpStatusCode statusCode = HttpStatus.OK;
        if (message instanceof ClientHttpResponse) {
            statusCode = ((ClientHttpResponse) message).getStatusCode();
        }
        HttpHeaders headers = message.getHeaders();
        int count = 0;
        String headerCount = headers.getFirst(Constants.HEADER_COUNT);
        if (StringUtils.hasText(headerCount)) {
            count = Integer.parseInt(headerCount);
        }
        return Map.of(
                "statusCode", statusCode,
                "count", count
        );
    }

    @Override
    protected @NonNull Map<String, Object> getReadHints(@NonNull ResolvableType actualType,
                                                        @NonNull ResolvableType elementType,
                                                        @NonNull ServerHttpRequest request,
                                                        @NonNull ServerHttpResponse response) {
        HttpStatusCode statusCode = response.getStatusCode();
        if (statusCode == null) {
            statusCode = HttpStatus.OK;
        }

        HttpHeaders headers = response.getHeaders();
        int count = 0;
        String headerCount = headers.getFirst(Constants.HEADER_COUNT);
        if (StringUtils.hasText(headerCount)) {
            count = Integer.parseInt(headerCount);
        }
        return Map.of(
                "statusCode", statusCode,
                "count", count
        );
    }
}
