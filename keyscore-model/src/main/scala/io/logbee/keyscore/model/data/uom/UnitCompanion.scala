package io.logbee.keyscore.model.data.uom

import io.logbee.keyscore.model.data.uom
import io.logbee.keyscore.model.data.uom.SystemOfUnits.SI.{Meter, Second}
import io.logbee.keyscore.model.data.uom.UnitConverterLike.UnitOps

import scala.language.implicitConversions

trait UnitCompanion {

  def apply(symbol: String) = new uom.Unit(symbol)

  //  def apply(symbol: String, quantity: Quantity) = new uom.Unit(symbol, None, Option(quantity))

  //  def apply(symbol: String, prefix: Prefix) = new uom.Unit(symbol, Some(prefix))

  def apply(symbol: String, prefix: Prefix, derived: Derived, deriveds: Derived*) = new uom.Unit(symbol, None, Option(prefix), (derived +: deriveds).toList)

  def apply(symbol: String, derived: Derived, deriveds: Derived*) = new uom.Unit(symbol, None, None, (derived +: deriveds).toList)

  trait ExtraImplicits {
    implicit def mkUnitOps(unit: uom.Unit): UnitOps[uom.Unit] = new UnitOps[uom.Unit](unit)
    implicit def mkUnitOps(unit: Option[uom.Unit]): UnitOps[Option[uom.Unit]] = new UnitOps[Option[uom.Unit]](unit)
  }

  object Implicits extends ExtraImplicits

  implicit object DefaultUnitConverter extends UnitConverterLike[uom.Unit] {

    override def plus(lhs: Unit, rhs: Unit): Unit = lhs

    override def minus(lhs: Unit, rhs: Unit): Unit = ???

    override def times(lhs: Unit, rhs: Unit): Unit = ???

    override def quot(lhs: Unit, rhs: Unit): Unit = ???

    override def rem(lhs: Unit, rhs: Unit): Unit = lhs
  }

  implicit object OptionUnitConverter extends UnitConverterLike[Option[uom.Unit]] {

    override def plus(lhs: Option[Unit], rhs: Option[Unit]): Option[Unit] = lhs

    override def minus(lhs: Option[Unit], rhs: Option[Unit]): Option[Unit] = lhs

    override def times(lhs: Option[Unit], rhs: Option[Unit]): Option[Unit] = Some(Unit(s"${lhs.get.symbol}*${rhs.get.symbol}", Derived(lhs.get, 1, 1), Derived(rhs.get, 1, 1)))

    override def quot(lhs: Option[Unit], rhs: Option[Unit]): Option[Unit] = Some(Unit(s"${lhs.get.symbol}/${rhs.get.symbol}", Derived(lhs.get, 1, 1), Derived(rhs.get, -1, 1)))

    override def rem(lhs: Option[Unit], rhs: Option[Unit]): Option[Unit] = lhs
  }
}
