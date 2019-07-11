package io.logbee.keyscore.model.data

import io.logbee.keyscore.model.data.NumericValue.NumericValueLike

import scala.language.implicitConversions

object NumberValue {

  def apply(value: Long = 0, unit: Option[uom.Unit] = None): NumberValue = new NumberValue(value, unit)

  def unapply(arg: NumberValue): Option[(Long, Option[uom.Unit])] = if (arg != null) Some(arg.value, arg.unit) else None

  implicit def toNativeValue(value: NumberValue): Value = NativeNumberValue(value.value, value.unit)

  implicit object NumberValueNumericValueLike extends NumericValueLike[NumberValue] {

    import io.logbee.keyscore.model.data.uom.Unit.Implicits._

    override def plus(lhs: NumberValue, rhs: NumberValue): NumberValue = NumberValue(lhs.value + rhs.value, lhs.unit + rhs.unit)

    override def minus(lhs: NumberValue, rhs: NumberValue): NumberValue = NumberValue(lhs.value - rhs.value, lhs.unit - rhs.unit)

    override def times(lhs: NumberValue, rhs: NumberValue): NumberValue = NumberValue(lhs.value * rhs.value, lhs.unit * rhs.unit)

    override def quot(lhs: NumberValue, rhs: NumberValue): NumberValue = NumberValue(lhs.value / rhs.value, lhs.unit / rhs.unit)

    override def rem(lhs: NumberValue, rhs: NumberValue): NumberValue = NumberValue(lhs.value % rhs.value, lhs.unit % rhs.unit)

    override def negate(lhs: NumberValue): NumberValue = NumberValue(lhs.value * -1, lhs.unit)

    override def fromInt(lhs: Int): NumberValue = NumberValue(lhs, None)

    override def toInt(lhs: NumberValue): Int = lhs.value.toInt

    override def toLong(lhs: NumberValue): Long = lhs.value

    override def toFloat(lhs: NumberValue): Float = lhs.value

    override def toDouble(lhs: NumberValue): Double = lhs.value
  }
}

class NumberValue(val value: Long, val unit: Option[uom.Unit]) extends NumericValue[NumberValue] {

  def canEqual(other: Any): Boolean = other.isInstanceOf[NumberValue]

  override def equals(other: Any): Boolean = other match {
    case that: NumberValue =>
      (that canEqual this) &&
        value == that.value &&
        unit == that.unit
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(value, unit)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }

  override def toString = s"NumberValue($value, $unit)"
}