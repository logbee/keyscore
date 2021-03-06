package io.logbee.keyscore.agent.util

import scala.collection.mutable


object RingBuffer {
  def apply[T](size: Int): RingBuffer[T] = new RingBuffer[T](size)
}

class RingBuffer[T](maxSize: Int) {

  private val ringBuffer = new Array[Any](maxSize)
  private var readPointer = 0
  private var writePointer = 0
  private var readableData = 0

  def push(element: T): Unit = {
    readableData = if (isFull) readableData else readableData + 1
    ringBuffer(writePointer) = element
    incrementWritePointer()
  }

  def pull(): T = {
    readableData = if (isEmpty) readableData else readableData - 1
    val element = ringBuffer(readPointer).asInstanceOf[T]
    incrementReadPointer()
    element
  }

  def take(n: Int): List[T] = {
    var takePointer = if (writePointer == 0) maxSize - 1 else writePointer - 1
    val result = mutable.ListBuffer.empty[T]

    for (_ <- 0 until readableData.min(n)) {
      result += ringBuffer(takePointer).asInstanceOf[T]
      takePointer = if (takePointer == 0) maxSize - 1 else takePointer - 1
    }
    result.toList
  }

  def last(number: Int): List[T] = new Iterator[T] {
    private var index = if (writePointer == 0) maxSize - 1 else writePointer - 1
    private var count = number.min(maxSize)
    override def hasNext: Boolean = count > 0 && ringBuffer(index) != null
    override def next(): T = {
      val result = ringBuffer(index).asInstanceOf[T]
      index = if (index > 0) index - 1 else maxSize - 1
      count -= 1
      result
    }
  }.toList

  def isNonEmpty: Boolean = {
    readableData > 0
  }

  def isNotFull: Boolean = {
    readableData < maxSize
  }

  def isFull: Boolean = {
    readableData == maxSize
  }

  def isEmpty: Boolean = {
    readableData == 0
  }

  def clear(): Unit = {
    readableData = 0
    readPointer = writePointer
  }

  def size: Int = readableData

  def limit: Int = maxSize

  private def incrementReadPointer() = {
    readPointer = if (readPointer == maxSize - 1) 0 else readPointer + 1
    readPointer
  }

  private def incrementWritePointer() = {
    writePointer = if (writePointer == maxSize - 1) 0 else writePointer + 1
    writePointer
  }
}
