package io.logbee.keyscore.agent.pipeline.stage

import akka.stream.{Inlet, Outlet, Shape}

import scala.annotation.unchecked.uncheckedVariance
import scala.collection.immutable

case class MergeShape[-L, -R, +O](left: Inlet[L @uncheckedVariance], right: Inlet[R @uncheckedVariance], out: Outlet[O @uncheckedVariance]) extends Shape {
  override val inlets: immutable.Seq[Inlet[_]] = left :: right :: Nil
  override val outlets: immutable.Seq[Outlet[_]] = out :: Nil

  override def deepCopy(): MergeShape[L, R, O] = MergeShape(left.carbonCopy(), right.carbonCopy(), out.carbonCopy())
}
