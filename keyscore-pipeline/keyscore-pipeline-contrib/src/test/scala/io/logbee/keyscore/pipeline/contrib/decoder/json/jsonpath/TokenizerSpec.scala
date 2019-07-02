package io.logbee.keyscore.pipeline.contrib.decoder.json.jsonpath

import io.logbee.keyscore.pipeline.contrib.decoder.json.jsonpath.Token.{IndexToken, NodeToken, RootToken, WildcardToken}
import io.logbee.keyscore.pipeline.contrib.decoder.json.jsonpath.Tokenizer.{IndexTokenizer, NodeTokenizer, RootTokenizer, WildcardTokenizer}
import org.junit.runner.RunWith
import org.scalatest.{FreeSpec, Matchers, OptionValues}
import org.scalatestplus.junit.JUnitRunner
import io.logbee.keyscore.model.util.ToOption.T2OptionT

@RunWith(classOf[JUnitRunner])
class TokenizerSpec extends FreeSpec with Matchers with OptionValues {

  case class TokenizerFixture(tokenizer: Tokenizer[_], path: String, expectedToken: Token, expectedTail: String)

  Seq(
    TokenizerFixture(
      tokenizer = RootTokenizer,
      path = "$.foo",
      expectedToken = RootToken(),
      expectedTail = ".foo"),

    TokenizerFixture(
      tokenizer = WildcardTokenizer,
      path = ".*.bar",
      expectedToken = WildcardToken(),
      expectedTail = ".bar"),

    TokenizerFixture(
      tokenizer = NodeTokenizer,
      path = ".foo",
      expectedToken = NodeToken("foo"),
      expectedTail = ""),

    TokenizerFixture(
      tokenizer = NodeTokenizer,
      path = ".foo.bar",
      expectedToken = NodeToken("foo"),
      expectedTail = ".bar"),

    TokenizerFixture(
      tokenizer = NodeTokenizer,
      path = ".foo[2:5].bar",
      expectedToken = NodeToken("foo"),
      expectedTail = "[2:5].bar"),

    TokenizerFixture(
      tokenizer = IndexTokenizer,
      path = "[1:4]",
      expectedToken = IndexToken(1, 4),
      expectedTail = ""),

    TokenizerFixture(
      tokenizer = IndexTokenizer,
      path = "[:5].foo",
      expectedToken = IndexToken(None, 5),
      expectedTail = ".foo"),

    TokenizerFixture(
      tokenizer = IndexTokenizer,
      path = "[3:].bar",
      expectedToken = IndexToken(3, None),
      expectedTail = ".bar"),

    TokenizerFixture(
      tokenizer = IndexTokenizer,
      path = "[-3:].bar",
      expectedToken = IndexToken(-3, None),
      expectedTail = ".bar"),
  )

  .foreach { case TokenizerFixture(tokenizer, path, expectedToken, expectedTail) =>

    s"A ${tokenizer.getClass.getSimpleName} should tokenize '$path' to token: '$expectedToken' and tail: '$expectedTail'" in {

      tokenizer.unapply(path).value shouldBe (expectedToken, expectedTail)
    }
  }
}
