package io.logbee.keyscore.model

import java.security.MessageDigest

import io.logbee.keyscore.model.DefaultEntityStore.{hasAncestor, isNotRootAncestor, update}
import io.logbee.keyscore.model.EntityStore.ROOT_ANCESTOR
import io.logbee.keyscore.model.util.Hex.toHexable

import scala.collection.mutable
import scala.collection.mutable.ListBuffer


object DefaultEntityStore {

  private def update(entity: Entity, ancestor: Option[String])(implicit digest: MessageDigest): Entity = {

    digest.reset()

    digest.update(entity.update(
      _.ref.revision := "",
      _.ref.ancestor := ancestor.getOrElse(entity.ref.ancestor)
    ).toByteArray)

    entity.update(
      _.ref.revision := digest.digest.toHex,
      _.ref.ancestor := ancestor.getOrElse(entity.ref.ancestor)
    )
  }

  private def hasAncestor(ref: EntityRef): Boolean = ref.ancestor != null && ref.ancestor.nonEmpty

  private def isNotRootAncestor(ref: EntityRef): Boolean = ref.ancestor != ROOT_ANCESTOR
}

class DefaultEntityStore extends EntityStore {

  private implicit val sha1: MessageDigest = MessageDigest.getInstance("SHA-1")
  private val index = mutable.HashMap.empty[String, mutable.ListBuffer[EntityRef]]
  private val store = mutable.HashMap.empty[String, Entity]

  def commit(entity: Entity): EntityRef = {

    val revisions = index.getOrElse(entity.ref.uuid, ListBuffer.empty[EntityRef])

    val updatedEntity = update(entity,
      ancestor = Option(if (revisions.isEmpty) ROOT_ANCESTOR else entity.ref.ancestor)
    )

    if (hasAncestor(updatedEntity.ref) && isNotRootAncestor(updatedEntity.ref)) {
      if (!revisions.exists(ref => ref.revision == updatedEntity.ref.ancestor)) {
        throw UnknownAncestorException(updatedEntity.ref)
      }
    }

    if (revisions.nonEmpty && updatedEntity.ref.revision == revisions.last.revision) {
      return updatedEntity.ref
    }

    if (revisions.nonEmpty && revisions.last.revision != updatedEntity.ref.ancestor) {
      throw DivergedException(
        base = store.getOrElse(revisions.last.ancestor, null),
        theirs = store(revisions.last.revision),
        yours = updatedEntity.update(
          _.ref.ancestor := revisions.last.ancestor
        )
      )
    }

    revisions += updatedEntity.ref

    index.put(updatedEntity.ref.uuid, revisions)
    store.put(updatedEntity.ref.revision, updatedEntity)

    updatedEntity.ref
  }

  def reset(ref: EntityRef): Unit = {

    index.get(ref.uuid) match {
      case Some(revisions) =>
        val count = revisions.indexWhere(other => other.revision == ref.revision)
        if (count >= 0) {
          for (_ <- 0 to count + 1) {
            store.remove(revisions.remove(revisions.size - 1).revision)
          }
        }
        else {
          throw UnknownRevisionException()
        }
      case _ => throw UnknownEntityException()
    }
  }
  
  def revert(ref: EntityRef): EntityRef = {
    index.get(ref.uuid) match {
      case Some(revisions) =>
        val revisionIndex = revisions.indexWhere(other => other.revision == ref.revision)
        if (revisionIndex == revisions.size - 1) {
          commit(store(revisions(revisionIndex - 1).revision)
            .update(
              _.ref.ancestor := revisions.last.revision
            ))
        }
        else if (revisionIndex > 0) {
          throw DivergedException(
            base = store(revisions.find(other => other.revision == ref.ancestor).get.revision),
            theirs = store(revisions.last.revision),
            yours = store(ref.revision)
          )
        }
        else if (revisionIndex == 0) {
          throw DivergedException(
            base = null,
            theirs = store(revisions.last.revision),
            yours = store(ref.revision)
          )
        }
        else {
          throw UnknownRevisionException()
        }
      case _ => throw UnknownEntityException()
    }
  }

  def delete(ref: EntityRef): Unit = {
    index.get(ref.uuid) match {
      case Some(revisions) =>
        revisions.foreach(ref => {
          store.remove(ref.revision)
        })
        index.remove(ref.uuid)
      case _ =>
    }
  }

  def head(): Seq[Entity] = {
    index.values
      .map(revisions => revisions.last)
      .map(last => store(last.revision))
      .toSeq
  }

  def head(ref: EntityRef): Option[Entity] = {

    if (!index.contains(ref.uuid)) {
      return None
    }

    index.get(ref.uuid).flatMap(revisions => store.get(revisions.last.revision))
  }

  def get(ref: EntityRef): Option[Entity] = {

    if (!index.contains(ref.uuid)) {
      return None
    }

    if (ref.revision == null || ref.revision.isEmpty) {
      return None
    }

    store.get(ref.revision)
  }

  def all(ref: EntityRef): Seq[Entity] = {
    index.getOrElse(ref.uuid, List.empty).foldLeft(List.empty[Entity]) { case (result, ref) =>
      store(ref.revision) +: result
    }
  }

  def clear(): Unit = {
    index.clear()
    store.clear()
  }
}
