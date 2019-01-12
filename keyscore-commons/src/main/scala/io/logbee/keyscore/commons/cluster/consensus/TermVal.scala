package io.logbee.keyscore.commons.cluster.consensus

trait TermVal {
  this: Term =>

  def previous: Term = this - 1
  def next: Term = this + 1

  def -(n: Long): Term = Term(value - n)
  def +(n: Long): Term = Term(value + n)

  def >(other: Term): Boolean = this.value > other.value
  def <(other: Term): Boolean = this.value < other.value

  def >=(other: Term): Boolean = this.value >= other.value
  def <=(other: Term): Boolean = this.value <= other.value
}
