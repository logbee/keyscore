package io.logbee.keyscore.model.util

object Using {
  def using[A <: AutoCloseable, B](resource: A)(f: A => B): B =
    try {
      f(resource)
    } finally {
      if (resource != null)
        resource.close()
    }
}
