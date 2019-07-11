package io.logbee.keyscore.model.data

import io.logbee.keyscore.model.data.NumericValue.NumericValueLike

import scala.language.implicitConversions
import scala.math.Numeric


trait NumericValue[T] {

  class NumericValueOps(lhs: T) {
    def +(rhs: T)(implicit ev: NumericValueLike[T]): T = ev.plus(lhs, rhs)
    def -(rhs: T)(implicit ev: NumericValueLike[T]): T = ev.minus(lhs, rhs)
    def *(rhs: T)(implicit ev: NumericValueLike[T]): T = ev.times(lhs, rhs)
    def /(rhs: T)(implicit ev: NumericValueLike[T]): T = ev.quot(lhs, rhs)
    def %(rhs: T)(implicit ev: NumericValueLike[T]): T = ev.rem(lhs, rhs)
    def unary_-()(implicit ev: NumericValueLike[T]): T = ev.negate(lhs)
    def toInt(implicit ev: NumericValueLike[T]): Int = ev.toInt(lhs)
    def toLong(implicit ev: NumericValueLike[T]): Long = ev.toLong(lhs)
    def toFloat(implicit ev: NumericValueLike[T]): Float = ev.toFloat(lhs)
    def toDouble(implicit ev: NumericValueLike[T]): Double = ev.toDouble(lhs)
  }
}

object NumericValue {

  import annotation.implicitNotFound

  object Implicits {
    implicit def mkNumericOps[T <: NumericValue[T]](lhs: T): NumericValue[T]#NumericValueOps = new lhs.NumericValueOps(lhs)
  }

  @implicitNotFound("No member of type class NumericValueLike in scope for ${T}")
  trait NumericValueLike[T] {

    def plus(lhs: T, rhs: T): T

    def minus(lhs: T, rhs: T): T

    def times(lhs: T, rhs: T): T

    def quot(lhs: T, rhs: T): T

    def rem(lhs: T, rhs: T): T

    def negate(lhs: T): T

    def fromInt(lhs: Int): T

    def toInt(lhs: T): Int

    def toLong(lhs: T): Long

    def toFloat(lhs: T): Float

    def toDouble(lhs: T): Double
  }
}