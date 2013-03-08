package net.mtgto.pediaroute.domain

class Query(from: String, to: String, way: Seq[String]) {
  private val pair: (String, String) = (from, to)

  override def hashCode: Int = {
    pair.hashCode
  }

  override def equals(o: Any): Boolean = {
    pair.equals(o)
  }

  override def toString: String = way.toString
}