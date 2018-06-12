package io.logbee.keyscore.agent.util


object Buffer {
  def apply[T](size: Int): Buffer[T] = new Buffer[T](size)
}

class Buffer[T](maxSize: Int) {

  private val ringBuffer = new Array[Any](maxSize)
  private var readPointer = 0
  private var writePointer = 0

  def push(element: T): Unit = {
    ringBuffer(writePointer) = element
    incrementWritePointer()
  }

  def pull(): T = {
      ringBuffer(nextRead()).asInstanceOf[T]
  }


  def take(n: Int): List[T] = {
    null
  }

  def isFull(): Boolean = {
    writePointer == readPointer
  }

  def isEmpty(): Boolean = {
    writePointer == readPointer
  }

  private def nextRead() = {
    readPointer = if (readPointer == maxSize - 1) 0 else readPointer + 1
    readPointer
  }

  private def incrementWritePointer() = {
    writePointer = if (writePointer == maxSize - 1) 0 else writePointer + 1
    writePointer
  }
}
