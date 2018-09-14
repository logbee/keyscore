package io.logbee.keyscore.agent.pipeline

import io.logbee.keyscore.model.blueprint.FilterBlueprint
import io.logbee.keyscore.model.blueprint.ToBlueprintRef._
import io.logbee.keyscore.test.fixtures.ProductionSystemWithMaterializerAndExecutionContext
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FreeSpecLike, Matchers}

class BlueprintMaterializerSpec extends ProductionSystemWithMaterializerAndExecutionContext with FreeSpecLike with Matchers with ScalaFutures {

  val exampleBlueprint = FilterBlueprint(
    ref = "76747e71-75db-4126-8cab-dd9afdde70fa",
    descriptor = "7dd6473a-194e-4e47-ad06-eedd9d67b195",
    configuration = "cbebe050-d092-4b98-b6dd-12849fb9aa8b"
  )

//  "A BlueprintMaterialzer" - {
//
//    val blueprintMaterializer = system.actorOf(BlueprintMaterializer(exampleBlueprint))
//
//    "should " in {
//
//    }
//  }
}
