package io.logbee.keyscore.commons.pipeline

import java.util.UUID

import io.logbee.keyscore.model.WhichValve
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.Dataset
import io.logbee.keyscore.model.pipeline.FilterState

case class PauseFilter(id: UUID, pause: Boolean)
case class PauseFilterResponse(state: FilterState)

case class DrainFilterValve(id:UUID, drain: Boolean)
case class DrainFilterResponse(state: FilterState)

case class InsertDatasets(id:UUID, datasets: List[Dataset], where: WhichValve)
case class InsertDatasetsResponse(state: FilterState)

case class ExtractDatasets(id:UUID, amount: Int, where: WhichValve)
case class ExtractDatasetsResponse(datasets: List[Dataset])

case class ConfigureFilter(id:UUID, configuration: Configuration)
case class ConfigureFilterResponse(state: FilterState)

case class CheckFilterState(id:UUID)
case class CheckFilterStateResponse(state: FilterState)

case class ClearBuffer(id:UUID)
case class ClearBufferResponse(state: FilterState)