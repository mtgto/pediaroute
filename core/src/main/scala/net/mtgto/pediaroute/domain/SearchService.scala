package net.mtgto.pediaroute.domain

import annotation.tailrec
import collection.mutable.{Set, HashSet, LinkedList}
import util.Random

class SearchService(
  titleMap: Map[String, Int], titleIdMap: Array[String], links: Array[Array[Int]], revLinks: Array[Array[Int]]) {
  private val MaxDepth = 6

  private def getIndexSet(word: String): Option[Set[Int]] = {
    titleMap.get(word).map { index =>
      HashSet[Int](index)
    }
  }

  def find(from: String, to: String): Option[Query] = {
    val startSet = getIndexSet(from)
    val goalSet = getIndexSet(to)
    (startSet, goalSet) match {
      case (None, _) => None
      case (_, None) => None
      case (Some(startSet), Some(goalSet)) => {
        // もし最近検索してればそこから返す
        find(startSet, goalSet, 0).map { list =>
          new Query(from, to, list.map(titleIdMap(_)))
        }
      }
    }
  }

  /**
   * みつからなかったらNoneを返す
   */
  def find(startSet: Set[Int], goalSet: Set[Int], depth: Int): Option[LinkedList[Int]] = {
    if (depth >= MaxDepth) {
      return None
    }
    if (startSet.size < goalSet.size) {
      val nextStartSet: Set[Int] = new HashSet[Int]
      startSet.foreach { from =>
        links(from).foreach { to =>
          if (goalSet.contains(to)) {
            return Some(LinkedList[Int](from, to))
          } else {
            nextStartSet += to
          }
        }
      }
      find(nextStartSet, goalSet, depth+1).map { way =>
        revLinks(way.head).map { index =>
          if (startSet.contains(index)) {
            return Some(index +: way)
          }
        }
      }
    } else {
      val nextGoalSet: Set[Int] = new HashSet[Int]
      goalSet.foreach { to =>
        revLinks(to).foreach { from =>
          if (startSet.contains(from)) {
            return Some(LinkedList[Int](from, to))
          } else {
            nextGoalSet += from
          }
        }
      }
      find(startSet, nextGoalSet, depth+1).map { way =>
        links(way.last).map { index =>
          if (goalSet.contains(index)) {
            return Some(way :+ index)
          }
        }
      }
    }
    return None
  }

  def getRandomQuery: Query = {
    new Query(getRandomWordFrom, getRandomWordTo, Seq.empty[String])
  }

  @tailrec
  private def getRandomWordFrom: String = {
    val index = Random.nextInt(titleIdMap.length)
    if (links(index).length > 0)
      titleIdMap(index)
    else
      getRandomWordFrom
  }

  @tailrec
  private def getRandomWordTo: String = {
    val index = Random.nextInt(titleIdMap.length)
    if (revLinks(index).length > 0)
      titleIdMap(index)
    else
      getRandomWordTo
  }
}