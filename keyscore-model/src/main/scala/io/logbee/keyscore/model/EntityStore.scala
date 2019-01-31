package io.logbee.keyscore.model

object EntityStore {
  val ROOT_ANCESTOR = "<root>"
}

/**
  * An EntityStore can manage revisions of [[Entity]]s.
  */
trait EntityStore {

  /** Adds the passed [[Entity]] to this [[EntityStore]].
    *
    * Based on the [[Entity]] and its [[EntityRef]] it is inserted as following:
    *
    * - If it is the first time the [[Entity]] is added to this [[EntityStore]]
    *   and the UUID of the [[Entity]] is therefore unknown, then the passed [[Entity]]
    *   shapes the root of the revisions for this [[Entity]].
    *
    * - If there is already a revision of the passed [[Entity]] and the [[EntityRef]]
    *   of the passed [[Entity]] does not specify an ancestor then the passes [[Entity]]
    *   is appended to the revisions and becomes the new ''head''.
    *
    * - If there is already a revision of the passed [[Entity]] and the [[EntityRef]]
    *   of the passed [[Entity]] does specify an ancestor and this ancestor does not point to
    *   the current ''head'' of revisions of passed [[Entity]] an [[DivergedException]] will be
    *   thrown.
    *
    * @example For a given Entity there are two revisions (A and B). When a Entity gets committed,
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
    *
    * @param Entity a [[Entity]]
    *
    * @return a [[EntityRef]] which points to the revision computed for the passed [[Entity]].
    */
  def commit(Entity: Entity): EntityRef

  /** Discard all revisions applied after the revision specified by the passed [[EntityRef]] of the denoted [[Entity]]
    *
    * @example For a given Entity there are four revisions (A to D). When the Entity gets reset to B,
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
    * @param ref a [[EntityRef]]
    *
    * @throws UnknownRevisionException if the revision specified in the passed [[EntityRef]] does not exist.
    * @throws UnknownEntityException if there is no [[Entity]] referenced by the passed [[EntityRef]].
    */
  def reset(ref: EntityRef): Unit

  /** Discard the revision specified by the passed [[EntityRef]] of the denoted [[Entity]] by re-applying
    * the ancestor revision.
    *
    * @example For a given Entity there are four revisions (A to C). When C gets reverted, then the revision B
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
    * @param ref a [[EntityRef]]
    *
    * @throws UnknownRevisionException if the revision specified in the passed [[EntityRef]] does not exist.
    * @throws UnknownEntityException if there is no [[Entity]] referenced by the passed [[EntityRef]].
    * @throws DivergedException if the ancestor could not be re-applied.
    */
  def revert(ref: EntityRef): EntityRef

  /** Deletes all revisions of the [[Entity]] denoted by the passed [[EntityRef]].
    *
    * @param ref a [[EntityRef]]
    */
  def delete(ref: EntityRef): Unit

  /** Returns the latest revision of all [[Entity]]s of this [[EntityStore]].
    *
    * @return a [[Seq]]
    */
  def head(): Seq[Entity]

  /** Returns the last committed revision (head) of the Entity denoted by the passed [[EntityRef]].
    *
    * In contrast to [[get]] this operation ignores the specified revision of the passed [[EntityRef]].
    *
    * @param ref a [[EntityRef]]
    *
    * @return The [[Entity]] for the given [[EntityRef]], otherwise None
    */
  def head(ref: EntityRef): Option[Entity]

  /** Returns the [[Entity]] denoted by the passed [[EntityRef]].
    *
    * In contrast to [[head]] this operation returns the revision of the [[Entity]] specified in the passed [[EntityRef]].
    *
    * @param ref a [[EntityRef]]
    *
    * @return The [[Entity]] for the given [[EntityRef]], otherwise None
    */
  def get(ref: EntityRef): Option[Entity]

  /** Returns all revisions of the [[Entity]] denoted by the passed [[EntityRef]]. The last committed
    * [[Entity]] is the first element (head) of the returned [[Seq]]. Where the last element of the [[Seq]] is
    * the first [[Entity]] committed (root).
    *
    * @param ref a [[EntityRef]]
    *
    * @return A [[Seq]] with all revisions of the [[Entity]] for the given [[EntityRef]], otherwise an empty [[Seq]].
    */
  def all(ref: EntityRef): Seq[Entity]

  /** Clears this [[EntityStore]].
    */
  def clear(): Unit
}

abstract class EntityStoreException extends RuntimeException
case class DivergedException(base: Entity, theirs: Entity, yours: Entity) extends EntityStoreException
case class UnknownRevisionException() extends EntityStoreException
case class UnknownAncestorException(ref: EntityRef) extends EntityStoreException
case class UnknownEntityException() extends EntityStoreException