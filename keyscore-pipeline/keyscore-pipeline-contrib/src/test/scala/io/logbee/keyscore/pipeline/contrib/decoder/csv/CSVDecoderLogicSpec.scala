package io.logbee.keyscore.pipeline.contrib.decoder.csv

import io.logbee.keyscore.model.configuration.{BooleanParameter, ChoiceParameter, _}
import io.logbee.keyscore.model.data._
import io.logbee.keyscore.model.descriptor.ToParameterRef.toRef
import io.logbee.keyscore.model.util.Using
import io.logbee.keyscore.pipeline.contrib.test.TestStreamFor
import io.logbee.keyscore.test.fixtures.TestSystemWithMaterializerAndExecutionContext
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FreeSpec, Matchers}

import scala.io.Source.fromInputStream

@RunWith(classOf[JUnitRunner])
class CSVDecoderLogicSpec extends FreeSpec with Matchers with ScalaFutures with MockFactory with TestSystemWithMaterializerAndExecutionContext {

  "A CSVDecoderLogic" - {

    "should return a MetaFilterDescriptor" in {
      CSVDecoderLogic.describe should not be null
    }

    "when configured in line mode" - {

      val sample = Dataset(
        Record(TextField("grades", "1;Math;2.5")),
        Record(TextField("grades", "2;Astrophysics;1.7"))
      )

      "and to keep the source field" - {

        val configuration = Configuration(
          parameterSet = ParameterSet(Seq(
            FieldNameParameter(CSVDecoderLogic.sourceFieldNameParameter, "grades"),
            TextParameter(CSVDecoderLogic.delimiterParameter, ";"),
            TextListParameter(CSVDecoderLogic.headerParameter, Seq("id", "subject", "grade")),
            BooleanParameter(CSVDecoderLogic.removeSourceFieldParameter, false)
          ))
        )

        "should convert a line into a record with fields with the source field" in new TestStreamFor[CSVDecoderLogic](configuration) {

          whenReady(filterFuture) { _ =>

            sink.request(1)
            source.sendNext(sample)

            val result = sink.requestNext()

            result.records should have size 2

            result.records.head.fields should contain allOf(
              Field("grades", TextValue("1;Math;2.5")),
              Field("id", TextValue("1")),
              Field("subject", TextValue("Math")),
              Field("grade", TextValue("2.5"))
            )

            result.records.last.fields should contain allOf(
              Field("grades", TextValue("2;Astrophysics;1.7")),
              Field("id", TextValue("2")),
              Field("subject", TextValue("Astrophysics")),
              Field("grade", TextValue("1.7"))
            )
          }
        }
      }

      "and to remove the source field" - {

        val configuration = Configuration(
          parameterSet = ParameterSet(Seq(
            FieldNameParameter(CSVDecoderLogic.sourceFieldNameParameter, "grades"),
            TextParameter(CSVDecoderLogic.delimiterParameter, ";"),
            TextListParameter(CSVDecoderLogic.headerParameter, Seq("id", "subject", "grade")),
            BooleanParameter(CSVDecoderLogic.removeSourceFieldParameter, true)
          ))
        )

        "should convert a line into a record with fields without the source field" in new TestStreamFor[CSVDecoderLogic](configuration) {

          whenReady(filterFuture) { _ =>

            sink.request(1)
            source.sendNext(sample)

            val result = sink.requestNext()

            result.records should have size 2

            result.records.head.fields should not contain Field("grades", TextValue("1;Math;2.5"))
            result.records.last.fields should not contain Field("grades", TextValue("2;Astrophysics;1.7"))
          }
        }
      }
    }

    "when configured in file mode" - {

      val sourceRecord = Using.using(getClass.getResourceAsStream("CSVDecoderLogicSpec.example.csv")) { stream =>
        Record(TextField("grades", fromInputStream(stream).mkString))
      }

      val sample = Dataset(sourceRecord)

      "and to keep the source field" - {

        val configuration = Configuration(
          parameterSet = ParameterSet(Seq(
            ChoiceParameter(CSVDecoderLogic.sourceFieldNameParameter, "grades"),
            ChoiceParameter(CSVDecoderLogic.modeParameter, CSVDecoderLogic.fileMode.name),
            TextParameter(CSVDecoderLogic.delimiterParameter, ","),
            TextListParameter(CSVDecoderLogic.headerParameter, Seq("id", "subject", "grade")),
            BooleanParameter(CSVDecoderLogic.removeSourceFieldParameter, false)
          ))
        )

        "should convert lines into records with fields with the source field" in new TestStreamFor[CSVDecoderLogic](configuration) {

          whenReady(filterFuture) { _ =>

            sink.request(1)
            source.sendNext(sample)

            val result = sink.requestNext()

            result.records should have size 5

            result.records should contain allOf(
              Record(TextField("id", "1"), TextField("subject", "Math"), TextField("grade", "2.5")),
              Record(TextField("id", "2"), TextField("subject", "Philosophy"), TextField("grade", "1.3")),
              Record(TextField("id", "3"), TextField("subject", "Astrophysics"), TextField("grade", "2")),
              Record(TextField("id", "4"), TextField("subject", "Latin"), TextField("grade", "3.7")),
              sourceRecord
            )
          }
        }
      }

      "and to remove the source field" - {

        val configuration = Configuration(
          parameterSet = ParameterSet(Seq(
            ChoiceParameter(CSVDecoderLogic.sourceFieldNameParameter, "grades"),
            ChoiceParameter(CSVDecoderLogic.modeParameter, CSVDecoderLogic.fileMode.name),
            TextParameter(CSVDecoderLogic.delimiterParameter, ","),
            TextListParameter(CSVDecoderLogic.headerParameter, Seq("id", "subject", "grade")),
            BooleanParameter(CSVDecoderLogic.removeSourceFieldParameter, true)
          ))
        )

        "should convert lines into records with fields without the source field" in new TestStreamFor[CSVDecoderLogic](configuration) {

          whenReady(filterFuture) { _ =>

            sink.request(1)
            source.sendNext(sample)

            val result = sink.requestNext()

            result.records should have size 4

            result.records should contain allOf(
              Record(TextField("id", "1"), TextField("subject", "Math"), TextField("grade", "2.5")),
              Record(TextField("id", "2"), TextField("subject", "Philosophy"), TextField("grade", "1.3")),
              Record(TextField("id", "3"), TextField("subject", "Astrophysics"), TextField("grade", "2")),
              Record(TextField("id", "4"), TextField("subject", "Latin"), TextField("grade", "3.7"))
            )

            result.records should not contain sourceRecord
          }
        }
      }
    }
  }
}
