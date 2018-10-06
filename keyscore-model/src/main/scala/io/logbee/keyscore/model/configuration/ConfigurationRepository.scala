package io.logbee.keyscore.model.configuration

import java.security.MessageDigest

import io.logbee.keyscore.model.configuration.ConfigurationRepository.{DivergedException, ROOT_ANCESTOR, UnknownConfigurationException, UnknownRevisionException}
import io.logbee.keyscore.model.util.Hex.toHexable

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

import io.logbee.keyscore.model.configuration.ConfigurationRepository._

object ConfigurationRepository {

  val ROOT_ANCESTOR = "<root>"

  case class DivergedException(base: Configuration, theirs: Configuration, yours: Configuration) extends RuntimeException
  case class UnknownRevisionException() extends RuntimeException
  case class UnknownConfigurationException() extends RuntimeException

  private def update(configuration: Configuration, ancestor: Option[String])(implicit digest: MessageDigest): Configuration = {

    digest.reset()

    digest.update(configuration.update(
      _.ref.revision := "",
      _.ref.ancestor := ancestor.getOrElse(configuration.ref.ancestor)
    ).toByteArray)

    configuration.update(
      _.ref.revision := digest.digest.toHex,
      _.ref.ancestor := ancestor.getOrElse(configuration.ref.ancestor)
    )
  }
}

/**
  * A ConfigurationRepository can manage revisions of [[Configuration]]s.
  */
class ConfigurationRepository {

  private implicit val sha1: MessageDigest = MessageDigest.getInstance("SHA-1")
  private val index = mutable.HashMap.empty[String, mutable.ListBuffer[ConfigurationRef]]
  private val store = mutable.HashMap.empty[String, Configuration]

  /** Adds the passed [[Configuration]] to this [[ConfigurationRepository]].
    *
    * Based on the [[Configuration]] and its [[ConfigurationRef]] it is inserted as following:
    *
    * - If it is the first time the [[Configuration]] is added to this [[ConfigurationRepository]]
    *   and the UUID of the [[Configuration]] is therefore unknown, then the passed [[Configuration]]
    *   shapes the root of the revisions for this [[Configuration]].
    *
    * - If there is already a revision of the passed [[Configuration]] and the [[ConfigurationRef]]
    *   of the passed [[Configuration]] does not specify an ancestor then the passes [[Configuration]]
    *   is appended to the revisions and becomes the new ''head''.
    *
    * - If there is already a revision of the passed [[Configuration]] and the [[ConfigurationRef]]
    *   of the passed [[Configuration]] does specify an ancestor and this ancestor does not point to
    *   the current ''head'' of revisions of passed [[Configuration]] an [[DivergedException]] will be
    *   thrown.
    *
    * @example For a given Configuration there are two revisions (A and B). When a configuration gets committed,
    *          then a new revision C is appended as new head.
    * {{{
    * [ditaa]
    * ....
    *  Before:
    *  +---+   +---+
    *  | A |-->| B |
    *  +---+   +---+
    *            ^
    *            |
    *          (head)
    *  After:
    *  +---+   +---+   +---+
    *  | A |-->| B |-->| C |
    *  +---+   +---+   +---+
    *                    ^
    *                    |
    *                  (head)
    * ....
    * }}}
    * @param configuration a [[Configuration]]
    *
    * @return a [[ConfigurationRef]] which points to the revision computed for the passed [[Configuration]].
    */
  def commit(configuration: Configuration): ConfigurationRef = {

    val revisions = index.getOrElse(configuration.ref.uuid, ListBuffer.empty[ConfigurationRef])

    revisions.find(ref => ref.ancestor == configuration.ref.ancestor).map(ref => {
      throw DivergedException(
        base = store(revisions.find(other => other.revision == ref.ancestor).get.revision),
        theirs = store(revisions.last.revision),
        yours = configuration
      )
    })

    val updatedConfiguration = update(configuration,
      ancestor = Option(revisions.lastOption.map(_.revision).getOrElse(ROOT_ANCESTOR))
    )

    revisions += updatedConfiguration.ref

    index.put(updatedConfiguration.ref.uuid, revisions)
    store.put(updatedConfiguration.ref.revision, updatedConfiguration)

    updatedConfiguration.ref
  }

  /** Discard all revisions applied after the revision specified by the passed [[ConfigurationRef]] of the denoted [[Configuration]]
    *
    * @example For a given Configuration there are four revisions (A to D). When the configuration gets rest to B,
    *          then the revisions C and D will be discarded.
    * {{{
    * [ditaa]
    * ....
    *  Before:
    *  +---+   +---+   +---+   +---+
    *  | A |-->| B |-->| C |-->| D |
    *  +---+   +---+   +---+   +---+
    *                            ^
    *                            |
    *                          (head)
    *  After:
    *  +---+   +---+
    *  | A |-->| B |
    *  +---+   +---+
    *            ^
    *            |
    *          (head)
    * ....
    * }}}
    * @param ref a [[ConfigurationRef]]
    *
    * @throws UnknownRevisionException if the revision specified in the passed [[ConfigurationRef]] does not exist.
    * @throws UnknownConfigurationException if there is no [[Configuration]] referenced by the passed [[ConfigurationRef]].
    */
  def reset(ref: ConfigurationRef): Unit = {

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
      case _ => throw UnknownConfigurationException()
    }
  }

  /** Discard the revision specified by the passed [[ConfigurationRef]] of the denoted [[Configuration]] by re-applying
    * the ancestor revision.
    *
    * @example For a given Configuration there are four revisions (A to C). When C gets reverted, then the revision B
    *          will be re-applied (B') and is therefore the new head.
    * {{{
    * [ditaa]
    * ....
    *  Before:
    *  +---+   +---+   +---+
    *  | A |-->| B |-->| C |
    *  +---+   +---+   +---+
    *                    ^
    *                    |
    *                  (head)
    *  After:
    *  +---+   +---+   +---+   +---+
    *  | A |-->| B |-->| C |-->| B'|
    *  +---+   +---+   +---+   +---+
    *                            ^
    *                            |
    *                          (head)
    * ....
    * }}}
    *
    * @param ref a [[ConfigurationRef]]
    *
    * @throws UnknownRevisionException if the revision specified in the passed [[ConfigurationRef]] does not exist.
    * @throws UnknownConfigurationException if there is no [[Configuration]] referenced by the passed [[ConfigurationRef]].
    * @throws DivergedException if the ancestor could not be re-applied.
    */
  def revert(ref: ConfigurationRef): ConfigurationRef = {
    index.get(ref.uuid) match {
      case Some(revisions) =>
        val revisionIndex = revisions.indexWhere(other => other.revision == ref.revision)
        if (revisionIndex == revisions.size - 1) {
          commit(store(revisions(revisionIndex - 1).revision)
            .update(
              _.ref.ancestor := ""
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
      case _ => throw UnknownConfigurationException()
    }
  }

  /** Remove all revisions of the [[Configuration]] denoted by the passed [[ConfigurationRef]].
    *
    * @param ref a [[ConfigurationRef]]
    */
  def remove(ref: ConfigurationRef): Unit = {
    index.get(ref.uuid) match {
      case Some(revisions) =>
        revisions.foreach(ref => {
          store.remove(ref.revision)
        })
        index.remove(ref.uuid)
      case _ =>
    }
  }

  /** Returns the latest revision of all [[Configuration]]s of this [[ConfigurationRepository]].
    *
    * @return a [[Seq]]
    */
  def head(): Seq[Configuration] = {
    index.values
      .map(revisions => revisions.last)
      .map(last => store(last.revision))
      .toSeq
  }

  /** Returns the last committed revision (head) of the configuration denoted by the passed [[ConfigurationRef]].
    *
    * In contrast to [[get]] this operation ignores the specified revision of the passed [[ConfigurationRef]].
    *
    * @param ref a [[ConfigurationRef]]
    *
    * @return The [[Configuration]] for the given [[ConfigurationRef]], otherwise None
    */
  def head(ref: ConfigurationRef): Option[Configuration] = {

    if (!index.contains(ref.uuid)) {
      return None
    }

    index.get(ref.uuid).flatMap(revisions => store.get(revisions.last.revision))
  }

  /** Returns the [[Configuration]] denoted by the passed [[ConfigurationRef]].
    *
    * In contrast to [[head]] this operation returns the revision of the [[Configuration]] specified in the passed [[ConfigurationRef]].
    *
    * @param ref a [[ConfigurationRef]]
    *
    * @return The [[Configuration]] for the given [[ConfigurationRef]], otherwise None
    */
  def get(ref: ConfigurationRef): Option[Configuration] = {

    if (!index.contains(ref.uuid)) {
      return None
    }

    if (ref.revision == null || ref.revision.isEmpty) {
      return None
    }

    store.get(ref.revision)
  }

  /** Returns all revisions of the [[Configuration]] denoted by the passed [[ConfigurationRef]]. The last committed
    * [[Configuration]] is the first element (head) of the returned [[Seq]]. Where the last element of the [[Seq]] is
    * the first [[Configuration]] committed (root).
    *
    * @param ref a [[ConfigurationRef]]
    *
    * @return A [[Seq]] with all revisions of the [[Configuration]] for the given [[ConfigurationRef]], otherwise an empty [[Seq]].
    */
  def all(ref: ConfigurationRef): Seq[Configuration] = {
    index.getOrElse(ref.uuid, List.empty).foldLeft(List.empty[Configuration]) { case (result, ref) =>
      store(ref.revision) +: result
    }
  }

  /** Clears this [[ConfigurationRepository]].
    */
  def clear(): Unit = {
    index.clear()
    store.clear()
  }
}
