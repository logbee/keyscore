package io.logbee.keyscore.model.data.uom

trait UnitConverterLike[T] {

  def plus(lhs: T, rhs: T): T

  def minus(lhs: T, rhs: T): T

  def times(lhs: T, rhs: T): T

  def quot(lhs: T, rhs: T): T

  def rem(lhs: T, rhs: T): T
}

object UnitConverterLike {

  class UnitOps[T](lhs: T) {

    def +(rhs: T)(implicit ev: UnitConverterLike[T]): T = ev.plus(lhs, rhs)

    def -(rhs: T)(implicit ev: UnitConverterLike[T]): T = ev.minus(lhs, rhs)

    def *(rhs: T)(implicit ev: UnitConverterLike[T]): T = ev.times(lhs, rhs)

    def /(rhs: T)(implicit ev: UnitConverterLike[T]): T = ev.quot(lhs, rhs)

    def %(rhs: T)(implicit ev: UnitConverterLike[T]): T = ev.rem(lhs, rhs)
  }
}
