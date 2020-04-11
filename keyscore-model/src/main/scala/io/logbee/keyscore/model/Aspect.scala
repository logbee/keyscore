package io.logbee.keyscore.model

trait AspectCompanion {

  def aspect(componentTypes: Class[_ <: Component]*): Aspect = Aspect(componentTypes.map(_.getName).toSet)

  def aspectOf(entity: Entity): Aspect = Aspect(entity.components.keySet)
}

trait BaseAspect {
  this: Aspect =>
}