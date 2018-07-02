package io.logbee.keyscore.model

import java.util.UUID

import io.logbee.keyscore.model.NativeModel.{NativeDataset, NativeField, NativeMetaData, NativeRecord}

import scala.collection.JavaConverters._

object NativeConversion {

  implicit def datasetToNative(dataset: Dataset): NativeDataset = {
    val builder = NativeDataset.newBuilder
    builder.setMetadata(dataset.metaData)
    builder.addAllRecord(dataset.records.map(recordToNative).asJava)
    builder.build()
  }

  implicit def datasetFromNative(native: NativeDataset): Dataset = {
    Dataset(MetaData(), native.getRecordList.asScala.map(recordFromNative).toList)
  }

  implicit def metadataToNative(metaData: MetaData): NativeMetaData = {
    val builder = NativeMetaData.newBuilder
    builder.build()
  }

  implicit def metadataFromNative(native: NativeMetaData): MetaData = {
    MetaData()
  }

  implicit def recordToNative(record: Record): NativeRecord = {
    val builder = NativeRecord.newBuilder
    builder.setId(record.id.toString)
    record.payload.values.foreach(builder.addField(_))
    builder.build()
  }

  implicit def recordFromNative(native: NativeRecord): Record = {
    Record(UUID.fromString(native.getId), native.getFieldList.asScala.map(fieldFromNative[Field[_]]).toList)
  }

  implicit def fieldToNative(field: Field[_]): NativeField = {
    val builder = NativeField.newBuilder
    builder.setName(field.name)
    builder.setKind(field.kind)
    field match {
      case TextField(_, text) => builder.setText(text)
      case NumberField(_, number) => builder.setNumber(number)
      case TimestampField(_, timestamp) => builder.setTimestamp(timestamp)
    }
    builder.build()
  }

  implicit def fieldFromNative[A <: Field[_]](native: NativeField): A = {
    native.getKind match {
      case "text" => TextField(native.getName, native.getText).asInstanceOf[A]
      case "number" => NumberField(native.getName, native.getNumber).asInstanceOf[A]
      case "timestamp" => TimestampField(native.getName, native.getTimestamp).asInstanceOf[A]
      case _ => throw new IllegalArgumentException(s"Could not convert NativeField due to unknown kind: '${native.getKind}'")
    }
  }
}
