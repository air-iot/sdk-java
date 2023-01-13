package cn.airiot.sdk.driver.grpc.driver;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.51.0)",
    comments = "Source: driver.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class DriverServiceGrpc {

  private DriverServiceGrpc() {}

  public static final String SERVICE_NAME = "driver.DriverService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<cn.airiot.sdk.driver.grpc.driver.HealthCheckRequest,
      cn.airiot.sdk.driver.grpc.driver.HealthCheckResponse> getHealthCheckMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "HealthCheck",
      requestType = cn.airiot.sdk.driver.grpc.driver.HealthCheckRequest.class,
      responseType = cn.airiot.sdk.driver.grpc.driver.HealthCheckResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<cn.airiot.sdk.driver.grpc.driver.HealthCheckRequest,
      cn.airiot.sdk.driver.grpc.driver.HealthCheckResponse> getHealthCheckMethod() {
    io.grpc.MethodDescriptor<cn.airiot.sdk.driver.grpc.driver.HealthCheckRequest, cn.airiot.sdk.driver.grpc.driver.HealthCheckResponse> getHealthCheckMethod;
    if ((getHealthCheckMethod = DriverServiceGrpc.getHealthCheckMethod) == null) {
      synchronized (DriverServiceGrpc.class) {
        if ((getHealthCheckMethod = DriverServiceGrpc.getHealthCheckMethod) == null) {
          DriverServiceGrpc.getHealthCheckMethod = getHealthCheckMethod =
              io.grpc.MethodDescriptor.<cn.airiot.sdk.driver.grpc.driver.HealthCheckRequest, cn.airiot.sdk.driver.grpc.driver.HealthCheckResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "HealthCheck"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cn.airiot.sdk.driver.grpc.driver.HealthCheckRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cn.airiot.sdk.driver.grpc.driver.HealthCheckResponse.getDefaultInstance()))
              .setSchemaDescriptor(new DriverServiceMethodDescriptorSupplier("HealthCheck"))
              .build();
        }
      }
    }
    return getHealthCheckMethod;
  }

  private static volatile io.grpc.MethodDescriptor<cn.airiot.sdk.driver.grpc.driver.Request,
      cn.airiot.sdk.driver.grpc.driver.Response> getEventMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Event",
      requestType = cn.airiot.sdk.driver.grpc.driver.Request.class,
      responseType = cn.airiot.sdk.driver.grpc.driver.Response.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<cn.airiot.sdk.driver.grpc.driver.Request,
      cn.airiot.sdk.driver.grpc.driver.Response> getEventMethod() {
    io.grpc.MethodDescriptor<cn.airiot.sdk.driver.grpc.driver.Request, cn.airiot.sdk.driver.grpc.driver.Response> getEventMethod;
    if ((getEventMethod = DriverServiceGrpc.getEventMethod) == null) {
      synchronized (DriverServiceGrpc.class) {
        if ((getEventMethod = DriverServiceGrpc.getEventMethod) == null) {
          DriverServiceGrpc.getEventMethod = getEventMethod =
              io.grpc.MethodDescriptor.<cn.airiot.sdk.driver.grpc.driver.Request, cn.airiot.sdk.driver.grpc.driver.Response>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Event"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cn.airiot.sdk.driver.grpc.driver.Request.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cn.airiot.sdk.driver.grpc.driver.Response.getDefaultInstance()))
              .setSchemaDescriptor(new DriverServiceMethodDescriptorSupplier("Event"))
              .build();
        }
      }
    }
    return getEventMethod;
  }

  private static volatile io.grpc.MethodDescriptor<cn.airiot.sdk.driver.grpc.driver.Request,
      cn.airiot.sdk.driver.grpc.driver.Response> getCommandLogMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "CommandLog",
      requestType = cn.airiot.sdk.driver.grpc.driver.Request.class,
      responseType = cn.airiot.sdk.driver.grpc.driver.Response.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<cn.airiot.sdk.driver.grpc.driver.Request,
      cn.airiot.sdk.driver.grpc.driver.Response> getCommandLogMethod() {
    io.grpc.MethodDescriptor<cn.airiot.sdk.driver.grpc.driver.Request, cn.airiot.sdk.driver.grpc.driver.Response> getCommandLogMethod;
    if ((getCommandLogMethod = DriverServiceGrpc.getCommandLogMethod) == null) {
      synchronized (DriverServiceGrpc.class) {
        if ((getCommandLogMethod = DriverServiceGrpc.getCommandLogMethod) == null) {
          DriverServiceGrpc.getCommandLogMethod = getCommandLogMethod =
              io.grpc.MethodDescriptor.<cn.airiot.sdk.driver.grpc.driver.Request, cn.airiot.sdk.driver.grpc.driver.Response>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "CommandLog"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cn.airiot.sdk.driver.grpc.driver.Request.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cn.airiot.sdk.driver.grpc.driver.Response.getDefaultInstance()))
              .setSchemaDescriptor(new DriverServiceMethodDescriptorSupplier("CommandLog"))
              .build();
        }
      }
    }
    return getCommandLogMethod;
  }

  private static volatile io.grpc.MethodDescriptor<cn.airiot.sdk.driver.grpc.driver.Request,
      cn.airiot.sdk.driver.grpc.driver.Response> getUpdateTableDataMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "UpdateTableData",
      requestType = cn.airiot.sdk.driver.grpc.driver.Request.class,
      responseType = cn.airiot.sdk.driver.grpc.driver.Response.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<cn.airiot.sdk.driver.grpc.driver.Request,
      cn.airiot.sdk.driver.grpc.driver.Response> getUpdateTableDataMethod() {
    io.grpc.MethodDescriptor<cn.airiot.sdk.driver.grpc.driver.Request, cn.airiot.sdk.driver.grpc.driver.Response> getUpdateTableDataMethod;
    if ((getUpdateTableDataMethod = DriverServiceGrpc.getUpdateTableDataMethod) == null) {
      synchronized (DriverServiceGrpc.class) {
        if ((getUpdateTableDataMethod = DriverServiceGrpc.getUpdateTableDataMethod) == null) {
          DriverServiceGrpc.getUpdateTableDataMethod = getUpdateTableDataMethod =
              io.grpc.MethodDescriptor.<cn.airiot.sdk.driver.grpc.driver.Request, cn.airiot.sdk.driver.grpc.driver.Response>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "UpdateTableData"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cn.airiot.sdk.driver.grpc.driver.Request.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cn.airiot.sdk.driver.grpc.driver.Response.getDefaultInstance()))
              .setSchemaDescriptor(new DriverServiceMethodDescriptorSupplier("UpdateTableData"))
              .build();
        }
      }
    }
    return getUpdateTableDataMethod;
  }

  private static volatile io.grpc.MethodDescriptor<cn.airiot.sdk.driver.grpc.driver.SchemaResult,
      cn.airiot.sdk.driver.grpc.driver.SchemaRequest> getSchemaStreamMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SchemaStream",
      requestType = cn.airiot.sdk.driver.grpc.driver.SchemaResult.class,
      responseType = cn.airiot.sdk.driver.grpc.driver.SchemaRequest.class,
      methodType = io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
  public static io.grpc.MethodDescriptor<cn.airiot.sdk.driver.grpc.driver.SchemaResult,
      cn.airiot.sdk.driver.grpc.driver.SchemaRequest> getSchemaStreamMethod() {
    io.grpc.MethodDescriptor<cn.airiot.sdk.driver.grpc.driver.SchemaResult, cn.airiot.sdk.driver.grpc.driver.SchemaRequest> getSchemaStreamMethod;
    if ((getSchemaStreamMethod = DriverServiceGrpc.getSchemaStreamMethod) == null) {
      synchronized (DriverServiceGrpc.class) {
        if ((getSchemaStreamMethod = DriverServiceGrpc.getSchemaStreamMethod) == null) {
          DriverServiceGrpc.getSchemaStreamMethod = getSchemaStreamMethod =
              io.grpc.MethodDescriptor.<cn.airiot.sdk.driver.grpc.driver.SchemaResult, cn.airiot.sdk.driver.grpc.driver.SchemaRequest>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SchemaStream"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cn.airiot.sdk.driver.grpc.driver.SchemaResult.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cn.airiot.sdk.driver.grpc.driver.SchemaRequest.getDefaultInstance()))
              .setSchemaDescriptor(new DriverServiceMethodDescriptorSupplier("SchemaStream"))
              .build();
        }
      }
    }
    return getSchemaStreamMethod;
  }

  private static volatile io.grpc.MethodDescriptor<cn.airiot.sdk.driver.grpc.driver.StartResult,
      cn.airiot.sdk.driver.grpc.driver.StartRequest> getStartStreamMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "StartStream",
      requestType = cn.airiot.sdk.driver.grpc.driver.StartResult.class,
      responseType = cn.airiot.sdk.driver.grpc.driver.StartRequest.class,
      methodType = io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
  public static io.grpc.MethodDescriptor<cn.airiot.sdk.driver.grpc.driver.StartResult,
      cn.airiot.sdk.driver.grpc.driver.StartRequest> getStartStreamMethod() {
    io.grpc.MethodDescriptor<cn.airiot.sdk.driver.grpc.driver.StartResult, cn.airiot.sdk.driver.grpc.driver.StartRequest> getStartStreamMethod;
    if ((getStartStreamMethod = DriverServiceGrpc.getStartStreamMethod) == null) {
      synchronized (DriverServiceGrpc.class) {
        if ((getStartStreamMethod = DriverServiceGrpc.getStartStreamMethod) == null) {
          DriverServiceGrpc.getStartStreamMethod = getStartStreamMethod =
              io.grpc.MethodDescriptor.<cn.airiot.sdk.driver.grpc.driver.StartResult, cn.airiot.sdk.driver.grpc.driver.StartRequest>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "StartStream"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cn.airiot.sdk.driver.grpc.driver.StartResult.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cn.airiot.sdk.driver.grpc.driver.StartRequest.getDefaultInstance()))
              .setSchemaDescriptor(new DriverServiceMethodDescriptorSupplier("StartStream"))
              .build();
        }
      }
    }
    return getStartStreamMethod;
  }

  private static volatile io.grpc.MethodDescriptor<cn.airiot.sdk.driver.grpc.driver.RunResult,
      cn.airiot.sdk.driver.grpc.driver.RunRequest> getRunStreamMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "RunStream",
      requestType = cn.airiot.sdk.driver.grpc.driver.RunResult.class,
      responseType = cn.airiot.sdk.driver.grpc.driver.RunRequest.class,
      methodType = io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
  public static io.grpc.MethodDescriptor<cn.airiot.sdk.driver.grpc.driver.RunResult,
      cn.airiot.sdk.driver.grpc.driver.RunRequest> getRunStreamMethod() {
    io.grpc.MethodDescriptor<cn.airiot.sdk.driver.grpc.driver.RunResult, cn.airiot.sdk.driver.grpc.driver.RunRequest> getRunStreamMethod;
    if ((getRunStreamMethod = DriverServiceGrpc.getRunStreamMethod) == null) {
      synchronized (DriverServiceGrpc.class) {
        if ((getRunStreamMethod = DriverServiceGrpc.getRunStreamMethod) == null) {
          DriverServiceGrpc.getRunStreamMethod = getRunStreamMethod =
              io.grpc.MethodDescriptor.<cn.airiot.sdk.driver.grpc.driver.RunResult, cn.airiot.sdk.driver.grpc.driver.RunRequest>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "RunStream"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cn.airiot.sdk.driver.grpc.driver.RunResult.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cn.airiot.sdk.driver.grpc.driver.RunRequest.getDefaultInstance()))
              .setSchemaDescriptor(new DriverServiceMethodDescriptorSupplier("RunStream"))
              .build();
        }
      }
    }
    return getRunStreamMethod;
  }

  private static volatile io.grpc.MethodDescriptor<cn.airiot.sdk.driver.grpc.driver.RunResult,
      cn.airiot.sdk.driver.grpc.driver.RunRequest> getWriteTagStreamMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "WriteTagStream",
      requestType = cn.airiot.sdk.driver.grpc.driver.RunResult.class,
      responseType = cn.airiot.sdk.driver.grpc.driver.RunRequest.class,
      methodType = io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
  public static io.grpc.MethodDescriptor<cn.airiot.sdk.driver.grpc.driver.RunResult,
      cn.airiot.sdk.driver.grpc.driver.RunRequest> getWriteTagStreamMethod() {
    io.grpc.MethodDescriptor<cn.airiot.sdk.driver.grpc.driver.RunResult, cn.airiot.sdk.driver.grpc.driver.RunRequest> getWriteTagStreamMethod;
    if ((getWriteTagStreamMethod = DriverServiceGrpc.getWriteTagStreamMethod) == null) {
      synchronized (DriverServiceGrpc.class) {
        if ((getWriteTagStreamMethod = DriverServiceGrpc.getWriteTagStreamMethod) == null) {
          DriverServiceGrpc.getWriteTagStreamMethod = getWriteTagStreamMethod =
              io.grpc.MethodDescriptor.<cn.airiot.sdk.driver.grpc.driver.RunResult, cn.airiot.sdk.driver.grpc.driver.RunRequest>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "WriteTagStream"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cn.airiot.sdk.driver.grpc.driver.RunResult.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cn.airiot.sdk.driver.grpc.driver.RunRequest.getDefaultInstance()))
              .setSchemaDescriptor(new DriverServiceMethodDescriptorSupplier("WriteTagStream"))
              .build();
        }
      }
    }
    return getWriteTagStreamMethod;
  }

  private static volatile io.grpc.MethodDescriptor<cn.airiot.sdk.driver.grpc.driver.BatchRunResult,
      cn.airiot.sdk.driver.grpc.driver.BatchRunRequest> getBatchRunStreamMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "BatchRunStream",
      requestType = cn.airiot.sdk.driver.grpc.driver.BatchRunResult.class,
      responseType = cn.airiot.sdk.driver.grpc.driver.BatchRunRequest.class,
      methodType = io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
  public static io.grpc.MethodDescriptor<cn.airiot.sdk.driver.grpc.driver.BatchRunResult,
      cn.airiot.sdk.driver.grpc.driver.BatchRunRequest> getBatchRunStreamMethod() {
    io.grpc.MethodDescriptor<cn.airiot.sdk.driver.grpc.driver.BatchRunResult, cn.airiot.sdk.driver.grpc.driver.BatchRunRequest> getBatchRunStreamMethod;
    if ((getBatchRunStreamMethod = DriverServiceGrpc.getBatchRunStreamMethod) == null) {
      synchronized (DriverServiceGrpc.class) {
        if ((getBatchRunStreamMethod = DriverServiceGrpc.getBatchRunStreamMethod) == null) {
          DriverServiceGrpc.getBatchRunStreamMethod = getBatchRunStreamMethod =
              io.grpc.MethodDescriptor.<cn.airiot.sdk.driver.grpc.driver.BatchRunResult, cn.airiot.sdk.driver.grpc.driver.BatchRunRequest>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "BatchRunStream"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cn.airiot.sdk.driver.grpc.driver.BatchRunResult.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cn.airiot.sdk.driver.grpc.driver.BatchRunRequest.getDefaultInstance()))
              .setSchemaDescriptor(new DriverServiceMethodDescriptorSupplier("BatchRunStream"))
              .build();
        }
      }
    }
    return getBatchRunStreamMethod;
  }

  private static volatile io.grpc.MethodDescriptor<cn.airiot.sdk.driver.grpc.driver.Debug,
      cn.airiot.sdk.driver.grpc.driver.Debug> getDebugStreamMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "DebugStream",
      requestType = cn.airiot.sdk.driver.grpc.driver.Debug.class,
      responseType = cn.airiot.sdk.driver.grpc.driver.Debug.class,
      methodType = io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
  public static io.grpc.MethodDescriptor<cn.airiot.sdk.driver.grpc.driver.Debug,
      cn.airiot.sdk.driver.grpc.driver.Debug> getDebugStreamMethod() {
    io.grpc.MethodDescriptor<cn.airiot.sdk.driver.grpc.driver.Debug, cn.airiot.sdk.driver.grpc.driver.Debug> getDebugStreamMethod;
    if ((getDebugStreamMethod = DriverServiceGrpc.getDebugStreamMethod) == null) {
      synchronized (DriverServiceGrpc.class) {
        if ((getDebugStreamMethod = DriverServiceGrpc.getDebugStreamMethod) == null) {
          DriverServiceGrpc.getDebugStreamMethod = getDebugStreamMethod =
              io.grpc.MethodDescriptor.<cn.airiot.sdk.driver.grpc.driver.Debug, cn.airiot.sdk.driver.grpc.driver.Debug>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "DebugStream"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cn.airiot.sdk.driver.grpc.driver.Debug.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cn.airiot.sdk.driver.grpc.driver.Debug.getDefaultInstance()))
              .setSchemaDescriptor(new DriverServiceMethodDescriptorSupplier("DebugStream"))
              .build();
        }
      }
    }
    return getDebugStreamMethod;
  }

  private static volatile io.grpc.MethodDescriptor<cn.airiot.sdk.driver.grpc.api.CreateRequest,
      cn.airiot.sdk.driver.grpc.api.Response> getBatchCommandMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "BatchCommand",
      requestType = cn.airiot.sdk.driver.grpc.api.CreateRequest.class,
      responseType = cn.airiot.sdk.driver.grpc.api.Response.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<cn.airiot.sdk.driver.grpc.api.CreateRequest,
      cn.airiot.sdk.driver.grpc.api.Response> getBatchCommandMethod() {
    io.grpc.MethodDescriptor<cn.airiot.sdk.driver.grpc.api.CreateRequest, cn.airiot.sdk.driver.grpc.api.Response> getBatchCommandMethod;
    if ((getBatchCommandMethod = DriverServiceGrpc.getBatchCommandMethod) == null) {
      synchronized (DriverServiceGrpc.class) {
        if ((getBatchCommandMethod = DriverServiceGrpc.getBatchCommandMethod) == null) {
          DriverServiceGrpc.getBatchCommandMethod = getBatchCommandMethod =
              io.grpc.MethodDescriptor.<cn.airiot.sdk.driver.grpc.api.CreateRequest, cn.airiot.sdk.driver.grpc.api.Response>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "BatchCommand"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cn.airiot.sdk.driver.grpc.api.CreateRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cn.airiot.sdk.driver.grpc.api.Response.getDefaultInstance()))
              .setSchemaDescriptor(new DriverServiceMethodDescriptorSupplier("BatchCommand"))
              .build();
        }
      }
    }
    return getBatchCommandMethod;
  }

  private static volatile io.grpc.MethodDescriptor<cn.airiot.sdk.driver.grpc.api.UpdateRequest,
      cn.airiot.sdk.driver.grpc.api.Response> getChangeCommandMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ChangeCommand",
      requestType = cn.airiot.sdk.driver.grpc.api.UpdateRequest.class,
      responseType = cn.airiot.sdk.driver.grpc.api.Response.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<cn.airiot.sdk.driver.grpc.api.UpdateRequest,
      cn.airiot.sdk.driver.grpc.api.Response> getChangeCommandMethod() {
    io.grpc.MethodDescriptor<cn.airiot.sdk.driver.grpc.api.UpdateRequest, cn.airiot.sdk.driver.grpc.api.Response> getChangeCommandMethod;
    if ((getChangeCommandMethod = DriverServiceGrpc.getChangeCommandMethod) == null) {
      synchronized (DriverServiceGrpc.class) {
        if ((getChangeCommandMethod = DriverServiceGrpc.getChangeCommandMethod) == null) {
          DriverServiceGrpc.getChangeCommandMethod = getChangeCommandMethod =
              io.grpc.MethodDescriptor.<cn.airiot.sdk.driver.grpc.api.UpdateRequest, cn.airiot.sdk.driver.grpc.api.Response>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ChangeCommand"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cn.airiot.sdk.driver.grpc.api.UpdateRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cn.airiot.sdk.driver.grpc.api.Response.getDefaultInstance()))
              .setSchemaDescriptor(new DriverServiceMethodDescriptorSupplier("ChangeCommand"))
              .build();
        }
      }
    }
    return getChangeCommandMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static DriverServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<DriverServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<DriverServiceStub>() {
        @java.lang.Override
        public DriverServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new DriverServiceStub(channel, callOptions);
        }
      };
    return DriverServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static DriverServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<DriverServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<DriverServiceBlockingStub>() {
        @java.lang.Override
        public DriverServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new DriverServiceBlockingStub(channel, callOptions);
        }
      };
    return DriverServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static DriverServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<DriverServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<DriverServiceFutureStub>() {
        @java.lang.Override
        public DriverServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new DriverServiceFutureStub(channel, callOptions);
        }
      };
    return DriverServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public static abstract class DriverServiceImplBase implements io.grpc.BindableService {

    /**
     */
    public void healthCheck(cn.airiot.sdk.driver.grpc.driver.HealthCheckRequest request,
        io.grpc.stub.StreamObserver<cn.airiot.sdk.driver.grpc.driver.HealthCheckResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getHealthCheckMethod(), responseObserver);
    }

    /**
     */
    public void event(cn.airiot.sdk.driver.grpc.driver.Request request,
        io.grpc.stub.StreamObserver<cn.airiot.sdk.driver.grpc.driver.Response> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getEventMethod(), responseObserver);
    }

    /**
     */
    public void commandLog(cn.airiot.sdk.driver.grpc.driver.Request request,
        io.grpc.stub.StreamObserver<cn.airiot.sdk.driver.grpc.driver.Response> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getCommandLogMethod(), responseObserver);
    }

    /**
     */
    public void updateTableData(cn.airiot.sdk.driver.grpc.driver.Request request,
        io.grpc.stub.StreamObserver<cn.airiot.sdk.driver.grpc.driver.Response> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getUpdateTableDataMethod(), responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<cn.airiot.sdk.driver.grpc.driver.SchemaResult> schemaStream(
        io.grpc.stub.StreamObserver<cn.airiot.sdk.driver.grpc.driver.SchemaRequest> responseObserver) {
      return io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall(getSchemaStreamMethod(), responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<cn.airiot.sdk.driver.grpc.driver.StartResult> startStream(
        io.grpc.stub.StreamObserver<cn.airiot.sdk.driver.grpc.driver.StartRequest> responseObserver) {
      return io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall(getStartStreamMethod(), responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<cn.airiot.sdk.driver.grpc.driver.RunResult> runStream(
        io.grpc.stub.StreamObserver<cn.airiot.sdk.driver.grpc.driver.RunRequest> responseObserver) {
      return io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall(getRunStreamMethod(), responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<cn.airiot.sdk.driver.grpc.driver.RunResult> writeTagStream(
        io.grpc.stub.StreamObserver<cn.airiot.sdk.driver.grpc.driver.RunRequest> responseObserver) {
      return io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall(getWriteTagStreamMethod(), responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<cn.airiot.sdk.driver.grpc.driver.BatchRunResult> batchRunStream(
        io.grpc.stub.StreamObserver<cn.airiot.sdk.driver.grpc.driver.BatchRunRequest> responseObserver) {
      return io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall(getBatchRunStreamMethod(), responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<cn.airiot.sdk.driver.grpc.driver.Debug> debugStream(
        io.grpc.stub.StreamObserver<cn.airiot.sdk.driver.grpc.driver.Debug> responseObserver) {
      return io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall(getDebugStreamMethod(), responseObserver);
    }

    /**
     */
    public void batchCommand(cn.airiot.sdk.driver.grpc.api.CreateRequest request,
        io.grpc.stub.StreamObserver<cn.airiot.sdk.driver.grpc.api.Response> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getBatchCommandMethod(), responseObserver);
    }

    /**
     */
    public void changeCommand(cn.airiot.sdk.driver.grpc.api.UpdateRequest request,
        io.grpc.stub.StreamObserver<cn.airiot.sdk.driver.grpc.api.Response> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getChangeCommandMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getHealthCheckMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                cn.airiot.sdk.driver.grpc.driver.HealthCheckRequest,
                cn.airiot.sdk.driver.grpc.driver.HealthCheckResponse>(
                  this, METHODID_HEALTH_CHECK)))
          .addMethod(
            getEventMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                cn.airiot.sdk.driver.grpc.driver.Request,
                cn.airiot.sdk.driver.grpc.driver.Response>(
                  this, METHODID_EVENT)))
          .addMethod(
            getCommandLogMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                cn.airiot.sdk.driver.grpc.driver.Request,
                cn.airiot.sdk.driver.grpc.driver.Response>(
                  this, METHODID_COMMAND_LOG)))
          .addMethod(
            getUpdateTableDataMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                cn.airiot.sdk.driver.grpc.driver.Request,
                cn.airiot.sdk.driver.grpc.driver.Response>(
                  this, METHODID_UPDATE_TABLE_DATA)))
          .addMethod(
            getSchemaStreamMethod(),
            io.grpc.stub.ServerCalls.asyncBidiStreamingCall(
              new MethodHandlers<
                cn.airiot.sdk.driver.grpc.driver.SchemaResult,
                cn.airiot.sdk.driver.grpc.driver.SchemaRequest>(
                  this, METHODID_SCHEMA_STREAM)))
          .addMethod(
            getStartStreamMethod(),
            io.grpc.stub.ServerCalls.asyncBidiStreamingCall(
              new MethodHandlers<
                cn.airiot.sdk.driver.grpc.driver.StartResult,
                cn.airiot.sdk.driver.grpc.driver.StartRequest>(
                  this, METHODID_START_STREAM)))
          .addMethod(
            getRunStreamMethod(),
            io.grpc.stub.ServerCalls.asyncBidiStreamingCall(
              new MethodHandlers<
                cn.airiot.sdk.driver.grpc.driver.RunResult,
                cn.airiot.sdk.driver.grpc.driver.RunRequest>(
                  this, METHODID_RUN_STREAM)))
          .addMethod(
            getWriteTagStreamMethod(),
            io.grpc.stub.ServerCalls.asyncBidiStreamingCall(
              new MethodHandlers<
                cn.airiot.sdk.driver.grpc.driver.RunResult,
                cn.airiot.sdk.driver.grpc.driver.RunRequest>(
                  this, METHODID_WRITE_TAG_STREAM)))
          .addMethod(
            getBatchRunStreamMethod(),
            io.grpc.stub.ServerCalls.asyncBidiStreamingCall(
              new MethodHandlers<
                cn.airiot.sdk.driver.grpc.driver.BatchRunResult,
                cn.airiot.sdk.driver.grpc.driver.BatchRunRequest>(
                  this, METHODID_BATCH_RUN_STREAM)))
          .addMethod(
            getDebugStreamMethod(),
            io.grpc.stub.ServerCalls.asyncBidiStreamingCall(
              new MethodHandlers<
                cn.airiot.sdk.driver.grpc.driver.Debug,
                cn.airiot.sdk.driver.grpc.driver.Debug>(
                  this, METHODID_DEBUG_STREAM)))
          .addMethod(
            getBatchCommandMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                cn.airiot.sdk.driver.grpc.api.CreateRequest,
                cn.airiot.sdk.driver.grpc.api.Response>(
                  this, METHODID_BATCH_COMMAND)))
          .addMethod(
            getChangeCommandMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                cn.airiot.sdk.driver.grpc.api.UpdateRequest,
                cn.airiot.sdk.driver.grpc.api.Response>(
                  this, METHODID_CHANGE_COMMAND)))
          .build();
    }
  }

  /**
   */
  public static final class DriverServiceStub extends io.grpc.stub.AbstractAsyncStub<DriverServiceStub> {
    private DriverServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected DriverServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new DriverServiceStub(channel, callOptions);
    }

    /**
     */
    public void healthCheck(cn.airiot.sdk.driver.grpc.driver.HealthCheckRequest request,
        io.grpc.stub.StreamObserver<cn.airiot.sdk.driver.grpc.driver.HealthCheckResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getHealthCheckMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void event(cn.airiot.sdk.driver.grpc.driver.Request request,
        io.grpc.stub.StreamObserver<cn.airiot.sdk.driver.grpc.driver.Response> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getEventMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void commandLog(cn.airiot.sdk.driver.grpc.driver.Request request,
        io.grpc.stub.StreamObserver<cn.airiot.sdk.driver.grpc.driver.Response> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getCommandLogMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void updateTableData(cn.airiot.sdk.driver.grpc.driver.Request request,
        io.grpc.stub.StreamObserver<cn.airiot.sdk.driver.grpc.driver.Response> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getUpdateTableDataMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<cn.airiot.sdk.driver.grpc.driver.SchemaResult> schemaStream(
        io.grpc.stub.StreamObserver<cn.airiot.sdk.driver.grpc.driver.SchemaRequest> responseObserver) {
      return io.grpc.stub.ClientCalls.asyncBidiStreamingCall(
          getChannel().newCall(getSchemaStreamMethod(), getCallOptions()), responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<cn.airiot.sdk.driver.grpc.driver.StartResult> startStream(
        io.grpc.stub.StreamObserver<cn.airiot.sdk.driver.grpc.driver.StartRequest> responseObserver) {
      return io.grpc.stub.ClientCalls.asyncBidiStreamingCall(
          getChannel().newCall(getStartStreamMethod(), getCallOptions()), responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<cn.airiot.sdk.driver.grpc.driver.RunResult> runStream(
        io.grpc.stub.StreamObserver<cn.airiot.sdk.driver.grpc.driver.RunRequest> responseObserver) {
      return io.grpc.stub.ClientCalls.asyncBidiStreamingCall(
          getChannel().newCall(getRunStreamMethod(), getCallOptions()), responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<cn.airiot.sdk.driver.grpc.driver.RunResult> writeTagStream(
        io.grpc.stub.StreamObserver<cn.airiot.sdk.driver.grpc.driver.RunRequest> responseObserver) {
      return io.grpc.stub.ClientCalls.asyncBidiStreamingCall(
          getChannel().newCall(getWriteTagStreamMethod(), getCallOptions()), responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<cn.airiot.sdk.driver.grpc.driver.BatchRunResult> batchRunStream(
        io.grpc.stub.StreamObserver<cn.airiot.sdk.driver.grpc.driver.BatchRunRequest> responseObserver) {
      return io.grpc.stub.ClientCalls.asyncBidiStreamingCall(
          getChannel().newCall(getBatchRunStreamMethod(), getCallOptions()), responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<cn.airiot.sdk.driver.grpc.driver.Debug> debugStream(
        io.grpc.stub.StreamObserver<cn.airiot.sdk.driver.grpc.driver.Debug> responseObserver) {
      return io.grpc.stub.ClientCalls.asyncBidiStreamingCall(
          getChannel().newCall(getDebugStreamMethod(), getCallOptions()), responseObserver);
    }

    /**
     */
    public void batchCommand(cn.airiot.sdk.driver.grpc.api.CreateRequest request,
        io.grpc.stub.StreamObserver<cn.airiot.sdk.driver.grpc.api.Response> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getBatchCommandMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void changeCommand(cn.airiot.sdk.driver.grpc.api.UpdateRequest request,
        io.grpc.stub.StreamObserver<cn.airiot.sdk.driver.grpc.api.Response> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getChangeCommandMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class DriverServiceBlockingStub extends io.grpc.stub.AbstractBlockingStub<DriverServiceBlockingStub> {
    private DriverServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected DriverServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new DriverServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public cn.airiot.sdk.driver.grpc.driver.HealthCheckResponse healthCheck(cn.airiot.sdk.driver.grpc.driver.HealthCheckRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getHealthCheckMethod(), getCallOptions(), request);
    }

    /**
     */
    public cn.airiot.sdk.driver.grpc.driver.Response event(cn.airiot.sdk.driver.grpc.driver.Request request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getEventMethod(), getCallOptions(), request);
    }

    /**
     */
    public cn.airiot.sdk.driver.grpc.driver.Response commandLog(cn.airiot.sdk.driver.grpc.driver.Request request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getCommandLogMethod(), getCallOptions(), request);
    }

    /**
     */
    public cn.airiot.sdk.driver.grpc.driver.Response updateTableData(cn.airiot.sdk.driver.grpc.driver.Request request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getUpdateTableDataMethod(), getCallOptions(), request);
    }

    /**
     */
    public cn.airiot.sdk.driver.grpc.api.Response batchCommand(cn.airiot.sdk.driver.grpc.api.CreateRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getBatchCommandMethod(), getCallOptions(), request);
    }

    /**
     */
    public cn.airiot.sdk.driver.grpc.api.Response changeCommand(cn.airiot.sdk.driver.grpc.api.UpdateRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getChangeCommandMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class DriverServiceFutureStub extends io.grpc.stub.AbstractFutureStub<DriverServiceFutureStub> {
    private DriverServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected DriverServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new DriverServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<cn.airiot.sdk.driver.grpc.driver.HealthCheckResponse> healthCheck(
        cn.airiot.sdk.driver.grpc.driver.HealthCheckRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getHealthCheckMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<cn.airiot.sdk.driver.grpc.driver.Response> event(
        cn.airiot.sdk.driver.grpc.driver.Request request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getEventMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<cn.airiot.sdk.driver.grpc.driver.Response> commandLog(
        cn.airiot.sdk.driver.grpc.driver.Request request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getCommandLogMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<cn.airiot.sdk.driver.grpc.driver.Response> updateTableData(
        cn.airiot.sdk.driver.grpc.driver.Request request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getUpdateTableDataMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<cn.airiot.sdk.driver.grpc.api.Response> batchCommand(
        cn.airiot.sdk.driver.grpc.api.CreateRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getBatchCommandMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<cn.airiot.sdk.driver.grpc.api.Response> changeCommand(
        cn.airiot.sdk.driver.grpc.api.UpdateRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getChangeCommandMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_HEALTH_CHECK = 0;
  private static final int METHODID_EVENT = 1;
  private static final int METHODID_COMMAND_LOG = 2;
  private static final int METHODID_UPDATE_TABLE_DATA = 3;
  private static final int METHODID_BATCH_COMMAND = 4;
  private static final int METHODID_CHANGE_COMMAND = 5;
  private static final int METHODID_SCHEMA_STREAM = 6;
  private static final int METHODID_START_STREAM = 7;
  private static final int METHODID_RUN_STREAM = 8;
  private static final int METHODID_WRITE_TAG_STREAM = 9;
  private static final int METHODID_BATCH_RUN_STREAM = 10;
  private static final int METHODID_DEBUG_STREAM = 11;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final DriverServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(DriverServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_HEALTH_CHECK:
          serviceImpl.healthCheck((cn.airiot.sdk.driver.grpc.driver.HealthCheckRequest) request,
              (io.grpc.stub.StreamObserver<cn.airiot.sdk.driver.grpc.driver.HealthCheckResponse>) responseObserver);
          break;
        case METHODID_EVENT:
          serviceImpl.event((cn.airiot.sdk.driver.grpc.driver.Request) request,
              (io.grpc.stub.StreamObserver<cn.airiot.sdk.driver.grpc.driver.Response>) responseObserver);
          break;
        case METHODID_COMMAND_LOG:
          serviceImpl.commandLog((cn.airiot.sdk.driver.grpc.driver.Request) request,
              (io.grpc.stub.StreamObserver<cn.airiot.sdk.driver.grpc.driver.Response>) responseObserver);
          break;
        case METHODID_UPDATE_TABLE_DATA:
          serviceImpl.updateTableData((cn.airiot.sdk.driver.grpc.driver.Request) request,
              (io.grpc.stub.StreamObserver<cn.airiot.sdk.driver.grpc.driver.Response>) responseObserver);
          break;
        case METHODID_BATCH_COMMAND:
          serviceImpl.batchCommand((cn.airiot.sdk.driver.grpc.api.CreateRequest) request,
              (io.grpc.stub.StreamObserver<cn.airiot.sdk.driver.grpc.api.Response>) responseObserver);
          break;
        case METHODID_CHANGE_COMMAND:
          serviceImpl.changeCommand((cn.airiot.sdk.driver.grpc.api.UpdateRequest) request,
              (io.grpc.stub.StreamObserver<cn.airiot.sdk.driver.grpc.api.Response>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_SCHEMA_STREAM:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.schemaStream(
              (io.grpc.stub.StreamObserver<cn.airiot.sdk.driver.grpc.driver.SchemaRequest>) responseObserver);
        case METHODID_START_STREAM:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.startStream(
              (io.grpc.stub.StreamObserver<cn.airiot.sdk.driver.grpc.driver.StartRequest>) responseObserver);
        case METHODID_RUN_STREAM:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.runStream(
              (io.grpc.stub.StreamObserver<cn.airiot.sdk.driver.grpc.driver.RunRequest>) responseObserver);
        case METHODID_WRITE_TAG_STREAM:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.writeTagStream(
              (io.grpc.stub.StreamObserver<cn.airiot.sdk.driver.grpc.driver.RunRequest>) responseObserver);
        case METHODID_BATCH_RUN_STREAM:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.batchRunStream(
              (io.grpc.stub.StreamObserver<cn.airiot.sdk.driver.grpc.driver.BatchRunRequest>) responseObserver);
        case METHODID_DEBUG_STREAM:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.debugStream(
              (io.grpc.stub.StreamObserver<cn.airiot.sdk.driver.grpc.driver.Debug>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class DriverServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    DriverServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return cn.airiot.sdk.driver.grpc.driver.Driver.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("DriverService");
    }
  }

  private static final class DriverServiceFileDescriptorSupplier
      extends DriverServiceBaseDescriptorSupplier {
    DriverServiceFileDescriptorSupplier() {}
  }

  private static final class DriverServiceMethodDescriptorSupplier
      extends DriverServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    DriverServiceMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (DriverServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new DriverServiceFileDescriptorSupplier())
              .addMethod(getHealthCheckMethod())
              .addMethod(getEventMethod())
              .addMethod(getCommandLogMethod())
              .addMethod(getUpdateTableDataMethod())
              .addMethod(getSchemaStreamMethod())
              .addMethod(getStartStreamMethod())
              .addMethod(getRunStreamMethod())
              .addMethod(getWriteTagStreamMethod())
              .addMethod(getBatchRunStreamMethod())
              .addMethod(getDebugStreamMethod())
              .addMethod(getBatchCommandMethod())
              .addMethod(getChangeCommandMethod())
              .build();
        }
      }
    }
    return result;
  }
}
