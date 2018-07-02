package io.logbee.keyscore.model

import akka.serialization.SerializerWithStringManifest
import io.logbee.keyscore.model.NativeModel.{NativeDataset, NativeField, NativeMetaData, NativeRecord}
import io.logbee.keyscore.model.NativeConversion._

class ModelSerializer extends SerializerWithStringManifest {

  def identifier: Int = 10619706


  override def manifest(o: AnyRef): String = o.getClass.getName

  final val Dataset = classOf[Dataset].getName
  final val MetaData = classOf[MetaData].getName
  final val Record = classOf[Record].getName
  final val TextField = classOf[TextField].getName
  final val NumberField = classOf[NumberField].getName
  final val TimestampField = classOf[TimestampField].getName

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef = manifest match {
    case Dataset => datasetFromNative(NativeDataset.parseFrom(bytes))
    case MetaData => metadataFromNative(NativeMetaData.parseFrom(bytes))
    case Record => recordFromNative(NativeRecord.parseFrom(bytes))
    case TextField | NumberField | TimestampField => fieldFromNative(NativeField.parseFrom(bytes))
  }

  override def toBinary(o: AnyRef): Array[Byte] = o match  {
    case dataset: Dataset => datasetToNative(dataset).toByteArray
    case metaData: MetaData =>  metadataToNative(metaData).toByteArray
    case record: Record =>  recordToNative(record).toByteArray
    case field: Field[_] => fieldToNative(field).toByteArray
  }
}