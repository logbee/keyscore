package io.logbee.keyscore.pipeline.contrib.decoder.json.jsonpath

import io.logbee.keyscore.pipeline.contrib.decoder.json.jsonpath.Token.{IndexToken, NodeToken, RootToken, WildcardToken}
import org.junit.runner.RunWith
import org.scalatest.{FreeSpec, Matchers, OptionValues}
import org.scalatestplus.junit.JUnitRunner
import io.logbee.keyscore.model.util.ToOption.T2OptionT

@RunWith(classOf[JUnitRunner])
class JsonPathSpec extends FreeSpec with Matchers with OptionValues {

  case class JsonPathEvaluationFixture(path: String, expectedTokens: List[Token])

  "A JsonPath" - {

    Seq(
      JsonPathEvaluationFixture(
        path = "$.foo[4:6].bar.*.id",
        expectedTokens = List(
          RootToken(),
          NodeToken("foo"),
          IndexToken(4, 6),
          NodeToken("bar"),
          WildcardToken(),
          NodeToken("id"),
        )
      )
    )

    .foreach { case JsonPathEvaluationFixture(path, expectedTokens) =>

      s"'$path' should be evaluated to: $expectedTokens" in {

        JsonPath(path).tokens should contain only (expectedTokens:_*)
      }
    }

    "should have a human-readable path to print" in {
      JsonPath("$.device.*.name").path shouldBe "$.device.*.name"
      JsonPath("$.device.logs[1:2]").path shouldBe "$.device.logs[1:2]"
    }
  }


}
