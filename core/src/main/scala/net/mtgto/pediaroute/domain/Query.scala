package net.mtgto.pediaroute.domain

class Query(val from: String, val to: String, val way: Seq[String]) {
  private val pair: (String, String) = (from, to)

  override def hashCode: Int = {
    pair.hashCode
  }

  override def equals(o: Any): Boolean = {
    pair.equals(o)
  }

  override def toString: String = way.toString
}