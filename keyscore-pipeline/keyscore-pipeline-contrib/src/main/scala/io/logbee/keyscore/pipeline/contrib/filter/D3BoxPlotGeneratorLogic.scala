package io.logbee.keyscore.pipeline.contrib.filter

import akka.stream.FlowShape
import akka.stream.stage.StageLogging
import io.logbee.keyscore.model.Described
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data._
import io.logbee.keyscore.model.descriptor.FieldNameHint.PresentField
import io.logbee.keyscore.model.descriptor._
import io.logbee.keyscore.model.localization.{Locale, Localization, TextRef}
import io.logbee.keyscore.model.util.ToOption.T2OptionT
import io.logbee.keyscore.pipeline.api.{FilterLogic, LogicParameters}
import io.logbee.keyscore.pipeline.commons.CommonCategories
import io.logbee.keyscore.pipeline.commons.CommonCategories.CATEGORY_LOCALIZATION
import io.logbee.keyscore.pipeline.contrib.filter.D3BoxPlotGeneratorLogic.{groupIdentifierParameter, itemIdentifierParameter}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.io.Source

object D3BoxPlotGeneratorLogic extends Described {
  private val iconName = "io.logbee.keyscore.pipeline.contrib.icon/box-plot.svg"

  private[filter] val groupIdentifierParameter = FieldNameParameterDescriptor(
    ref = "d3BoxPlot.groupIdentifier",
    ParameterInfo(TextRef("groupIdentifierDisplayName"), TextRef("groupIdentifierDescription")),
    hint = PresentField,
    defaultValue = "group",
    mandatory = true
  )

  private[filter] val itemIdentifierParameter = FieldNameParameterDescriptor(
    ref = "d3BoxPlot.itemIdentifier",
    ParameterInfo(TextRef("itemIdentifierDisplayName"), TextRef("itemIdentifierDescription")),
    hint = PresentField,
    defaultValue = "item",
    mandatory = true
  )

  override def describe = Descriptor(
      ref = "b005f9e7-a4bd-4136-aee5-f2b1c02cc712",
      describes = FilterDescriptor(
        name = classOf[D3BoxPlotGeneratorLogic].getName,
        displayName = TextRef("displayName"),
        description = TextRef("description"),
        categories = Seq(CommonCategories.VISUALIZATION),
        parameters = Seq(
          groupIdentifierParameter,
          itemIdentifierParameter
        ),
        icon = Icon.fromClass(classOf[D3BoxPlotGeneratorLogic]),
        maturity = Maturity.Experimental
      ),
      localization = Localization.fromResourceBundle(
        bundleName = "io.logbee.keyscore.pipeline.contrib.filter.D3BoxPlotGenerator",
        Locale.ENGLISH, Locale.GERMAN
      ) ++ CATEGORY_LOCALIZATION
    )
}

class D3BoxPlotGeneratorLogic(parameters: LogicParameters, shape: FlowShape[Dataset, Dataset]) extends FilterLogic(parameters, shape) with StageLogging {

  //DO NOT CHANGE THESE UNLESS YOU ARE CHANGING THE VARIABLES IN THE d3_boxplot.htlm FILE
  private val i = "var groupToListOfNumbers ="
  private val n = "var allNumbers ="

  private val fieldName = "d3_boxplot"

  //IMPORTANT INFORMATION: At this point, this block takes only NumberValues (Longs) as data types.
  private var groupIdentifier: String = groupIdentifierParameter.defaultValue // Name of the field that groups the item values (e.g. timestamp, groupID, roboNumber ...)
  private var itemIdentifier: String = itemIdentifierParameter.defaultValue // Name of the field that contains the value for the BoxPlot (e.g. value, height, temp ...)
  private var groupType: String = "Long" // Type of the group value (e.g. Long, Double, String)
  private var itemType: String = "Long" // Type of the item value (.e.g. Long, Double)

  private var allItems: mutable.Map[Value, ListBuffer[Value]] = mutable.Map.empty[Value, ListBuffer[Value]]
  private var allItemValues = ListBuffer.empty[Value]

  private var stringifiedItems = ""
  private var stringifiedValues = ""

  override def initialize(configuration: Configuration): Unit = {
    configure(configuration)
  }

  override def configure(configuration: Configuration): Unit = {
    groupIdentifier = configuration.getValueOrDefault(groupIdentifierParameter, groupIdentifier)
    itemIdentifier = configuration.getValueOrDefault(itemIdentifierParameter, itemIdentifier)
  }

  override def onPull(): Unit = {
    pull(in)
  }


  override def onPush(): Unit = {
    val dataset = grab(in)
    for (record <- dataset.records) {
      checkGroup(record)
    }

    generateStrings

    val html = generateHTML
    val d = Dataset(Record(Field(fieldName, TextValue(html))))

    push(out, d)
  }

  private def checkGroup(record: Record) = {
    val group = groupIdentifier
    record.fields.find {
      case Field(`group`, NumberValue(_, _)) => true
      case _ => false
    }.map(field => {
      if (!allItems.contains(field.value)) {
        allItems += (field.value -> ListBuffer.empty[Value])
      }
      addItemValue(record, field)
    }
    )
  }

  private def addItemValue(record: Record, f: Field) = {
    val item = itemIdentifier
    record.fields.find {
      case Field(`item`, NumberValue(_, _)) => true
      case _ => false
    }.map(valueField => {
      allItems += (f.value -> (allItems(f.value) += valueField.value))
      allItemValues += valueField.value
    }
    )
  }

  private def parseLongMap(m: mutable.Map[Value, ListBuffer[Value]]): Map[Long, List[Long]] = {
    var numberMap: mutable.LinkedHashMap[Long, List[Long]] = mutable.LinkedHashMap.empty[Long, List[Long]]
    for (elem <- m) {
      numberMap += (parseToLong(elem._1) -> parseListToLong(elem._2))
    }

    numberMap.toMap
  }

  private def parseToLong(v: Value): Long = {
    v.asMessage.getNumber.value
  }

  private def parseListToLong(l: ListBuffer[Value]): List[Long] = {
    var listOfLongs: ListBuffer[Long] = ListBuffer.empty[Long]

    l.foreach {
      case num: NumberValue => listOfLongs += num.value
      case _ =>
    }

    listOfLongs.toList
  }

  private def generateStrings: Unit = {
    val items = parseLongMap(allItems)
    val values = parseListToLong(allItemValues)

    //"Map(1 -> List(1, 2, 3), 1 -> List(4, 5, 6))" ==> "{1:[1,2,3],2:[4,5,6]}"
    stringifiedItems = s"{${items.head._1}:[" + items.head._2.head + items.head._2.tail.foldLeft("") { case (r, e) => s"$r,$e" } + "]"
    items.tail.foreach { e =>
      stringifiedItems = stringifiedItems + s",${e._1}:[${e._2.head}" + e._2.tail.foldLeft("") { case (r, e) => s"$r,$e" } + "]"
    }
    stringifiedItems = stringifiedItems + "}"

    //"List(1, 2 , 3, 4, 5)" ==> "[1,2,3,4,5]"
    stringifiedValues = s"[${values.head}" + values.tail.foldLeft("") { case (result, element) => s"$result,$element" } + "]"

  }

  def generateHTML: String = {
    var htmlString = ""

    Source.fromResource(s"$fieldName.html")
      .getLines()
      .map { line =>
        if (line.contains(i)) {
          s"$i $stringifiedItems"
        } else if (line.contains(n)) {
          s"$n $stringifiedValues"
        } else {
          line
        }
      }
      .foreach { line =>
        htmlString = htmlString + "\n" + line
      }

    htmlString
  }
}
