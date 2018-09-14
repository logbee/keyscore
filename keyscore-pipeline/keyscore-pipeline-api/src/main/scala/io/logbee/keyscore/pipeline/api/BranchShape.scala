package io.logbee.keyscore.pipeline.api

import akka.stream.{Inlet, Outlet, Shape}

import scala.annotation.unchecked.uncheckedVariance
import scala.collection.immutable

case class BranchShape[-I, +L, +R](in: Inlet[I @uncheckedVariance], left: Outlet[L @uncheckedVariance], right: Outlet[R @uncheckedVariance]) extends Shape {
  override val inlets: immutable.Seq[Inlet[_]] = in :: Nil
  override val outlets: immutable.Seq[Outlet[_]] = left :: right :: Nil

  override def deepCopy(): BranchShape[I, L, R] = BranchShape(in.carbonCopy(), left.carbonCopy(), right.carbonCopy())
}
