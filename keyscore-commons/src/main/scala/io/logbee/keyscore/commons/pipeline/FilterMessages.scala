package io.logbee.keyscore.commons.pipeline

import java.util.UUID

import io.logbee.keyscore.model.Dataset
import io.logbee.keyscore.model.filter.FilterConfiguration

case class PauseFilter(id: UUID, pause: Boolean)
case class DrainFilterValve(id:UUID, drain: Boolean)
case class InsertDatasets(id:UUID, datasets: List[Dataset])

case class ExtractDatasets(id:UUID, amount: Int)
case class ExtractDatasetsResponse(datasets: List[Dataset])
case class ConfigureFilter(id:UUID, configuration: FilterConfiguration)
