package io.logbee.keyscore.model.conversion

import io.logbee.keyscore.model._

trait MetaDataConversion {

  implicit def toAdvancedMetaData(metaData: MetaData): AdvancedMetaData = {
    AdvancedMetaData(metaData)
  }

  implicit def fromAdvancedMetaData(advancedMetaData: AdvancedMetaData): MetaData = {
    advancedMetaData.metaData
  }
}

trait LabelConversion {

  implicit def toAdvancedLabel(label: Label): AdvancedLabel[_] = {
    AdvancedLabel(label)
  }

  implicit def fromAdvancedLabel(advancedLabel: AdvancedLabel[_]): Label = {
    advancedLabel.label
  }
}


