package io.logbee.keyscore.commons.pipeline

import java.util.UUID

import io.logbee.keyscore.model.Dataset
import io.logbee.keyscore.model.filter.{FilterConfiguration, FilterState}

case class PauseFilter(id: UUID, pause: Boolean)
case class PauseFilterResponse(state: FilterState)

case class DrainFilterValve(id:UUID, drain: Boolean)
case class DrainFilterResponse(state: FilterState)

case class InsertDatasets(id:UUID, datasets: List[Dataset])
case class InsertDatasetsResponse(state: FilterState)

case class ExtractDatasets(id:UUID, amount: Int)
case class ExtractDatasetsResponse(datasets: List[Dataset])

case class ConfigureFilter(id:UUID, configuration: FilterConfiguration)
case class ConfigureFilterResponse(state: FilterState)

case class CheckFilterState(id:UUID)
case class CheckFilterStateResponse(state: FilterState)