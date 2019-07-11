package io.logbee.keyscore.model.data.uom

import io.logbee.keyscore.model.data.uom.SystemOfUnits.IEEE.Byte
import org.scalatest.{FreeSpec, Matchers}
import io.logbee.keyscore.model.data.{Field, NumberValue, uom}


class UnitOfMeasurementSpec extends FreeSpec with Matchers {

  /**
    * - dimensionless  unit  like  percen,
    * - count  with  proper  prefixes  or  multipliers: millions,  thousands
    * - monetary units: $
    *
    */

  "fubar" in {

    import io.logbee.keyscore.model.data.uom.SystemOfUnits.SI._
    import io.logbee.keyscore.model.util.ToOption.T2OptionT
    import io.logbee.keyscore.model.data.NumericValue.Implicits._
    import io.logbee.keyscore.model.data.uom.Unit.Implicits._

    val MeterPerSecond = Unit("m/s", Derived(Meter, 1, 1), Derived(Second, -1, 1))
    val SquareMeter = Unit("m*m", Derived(Meter, 1, 1), Derived(Meter, 1, 1))
    val Newton = Unit("N", Derived(KiloGram, 1, 1), Derived(Meter, 1, 1), Derived(Second, -2, 1))
    val Percent = Unit("%")

    val field1 = Field("x", NumberValue(17, Newton))
    val field2 = Field("x", NumberValue(32, MeterPerSecond))
    val field3 = Field("x", NumberValue(4, Percent))
    val field4 = Field("x", NumberValue(42, Byte))

    val a = NumberValue(42, Meter)
    val b = NumberValue(5, Meter)
    val c = NumberValue(5, Second)

    a + b shouldBe NumberValue(47, Meter)
    a - b shouldBe NumberValue(37, Meter)
    a * b shouldBe NumberValue(210, SquareMeter)
    a / c shouldBe NumberValue(8, MeterPerSecond)
    a % b shouldBe NumberValue(2, Meter)
    -a shouldBe NumberValue(-42, Meter)

    println(s"$field1")
    println(s"$field2")
    println(s"$field3")
    println(s"$field4")
  }
}
