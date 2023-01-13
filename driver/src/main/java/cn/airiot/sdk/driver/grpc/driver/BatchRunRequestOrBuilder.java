// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: driver.proto

package cn.airiot.sdk.driver.grpc.driver;

public interface BatchRunRequestOrBuilder extends
    // @@protoc_insertion_point(interface_extends:driver.BatchRunRequest)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>string request = 1;</code>
   * @return The request.
   */
  String getRequest();
  /**
   * <code>string request = 1;</code>
   * @return The bytes for request.
   */
  com.google.protobuf.ByteString
      getRequestBytes();

  /**
   * <code>string tableId = 2;</code>
   * @return The tableId.
   */
  String getTableId();
  /**
   * <code>string tableId = 2;</code>
   * @return The bytes for tableId.
   */
  com.google.protobuf.ByteString
      getTableIdBytes();

  /**
   * <code>repeated string id = 3;</code>
   * @return A list containing the id.
   */
  java.util.List<String>
      getIdList();
  /**
   * <code>repeated string id = 3;</code>
   * @return The count of id.
   */
  int getIdCount();
  /**
   * <code>repeated string id = 3;</code>
   * @param index The index of the element to return.
   * @return The id at the given index.
   */
  String getId(int index);
  /**
   * <code>repeated string id = 3;</code>
   * @param index The index of the value to return.
   * @return The bytes of the id at the given index.
   */
  com.google.protobuf.ByteString
      getIdBytes(int index);

  /**
   * <code>string serialNo = 4;</code>
   * @return The serialNo.
   */
  String getSerialNo();
  /**
   * <code>string serialNo = 4;</code>
   * @return The bytes for serialNo.
   */
  com.google.protobuf.ByteString
      getSerialNoBytes();

  /**
   * <code>bytes command = 5;</code>
   * @return The command.
   */
  com.google.protobuf.ByteString getCommand();
}
