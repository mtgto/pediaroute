package net.mtgto.pediaroute.domain

import collection.mutable.{Map, HashMap, Set, HashSet, LinkedList}

class SearchService {
  private val MaxDepth = 6

  private val titleMap: Map[String, Int] = Map("東京" -> 0, "大学" -> 1, "高尾山" -> 2, "紅葉狩り" -> 3, "文京区" -> 4, "本郷" -> 5)

  private val titleIdMap: Array[String] = Array("東京", "大学", "高尾山", "紅葉狩り", "文京区", "本郷")

  private val links: Array[Array[Int]] = Array(Array(2,4), Array(), Array(0,3), Array(), Array(0,5), Array(1))

  private val revLinks: Array[Array[Int]] = Array(Array(2,4), Array(5), Array(0), Array(2), Array(0), Array(4))

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
}