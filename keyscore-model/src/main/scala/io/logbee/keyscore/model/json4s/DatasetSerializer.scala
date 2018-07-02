package io.logbee.keyscore.model.json4s

import io.logbee.keyscore.model.{Dataset, MetaData, Record}
import org.json4s.CustomSerializer
import org.json4s.JsonAST.JObject
import org.json4s.JsonDSL._
import org.json4s.JsonAST._


/*class DatasetSerializer extends CustomSerializer[Dataset](format => ( {
  case jsonObj: JObject =>
    val metaData = (jsonObj \ "metaData").extract[MetaData]
    val records = (jsonObj \ "records").extract[List[Record]]

    Dataset(metaData, records)

}, {
  case dataset: Dataset =>
    ("metaData" -> dataset.metaData.labels.map{ l =>
      (("label-name" -> l))
    })~
      ("records" -> dataset.records)
}))*/
