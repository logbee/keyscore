package io.logbee.keyscore.model.data.math

import io.logbee.keyscore.model.data.{DecimalValue, NumberValue}

object Math {

  trait NumberLike[T] {
//    def plus(x: T, y: T): T
//    def divide(x: T, y: Int): T
//    def minus(x: T, y: T): T
  }

  object NumberLike {

    implicit object NumberLikeNumberValue extends NumberLike[NumberValue] {
//      def plus(x: NumberValue, y: NumberValue): NumberValue = x + y
//      def divide(x: NumberValue, y: NumberValue): NumberValue = x / y
//      def minus(x: NumberValue, y: NumberValue): NumberValue = x - y
    }

    implicit object NumberLikeDecimalValue extends NumberLike[DecimalValue] {
//      def plus(x: DecimalValue, y: DecimalValue): DecimalValue = x + y
//      def divide(x: DecimalValue, y: DecimalValue): DecimalValue = x / y
//      def minus(x: DecimalValue, y: DecimalValue): DecimalValue = x - y
    }
  }

  def mean[T](xs: Vector[T])(implicit ev: NumberLike[T]): T = ???
//    ev.divide(xs.reduce(ev.plus(_, _)), xs.size)

}

