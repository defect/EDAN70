object MutualInformation extends Redis {
  var memo:Map[String,Int] = Map()

  /* Calculate top n labels based on the mutual information between the cluster
   * and the term */
  def labels(cluster: String, n: Int): List[Tuple2[String, Double]] = {
    redis.smembers[String](s"cluster:tags:$cluster").get.map({item =>
      item.get
    }).map({ term =>  // Calculate MI for each term
      Tuple2(term, mi(cluster, term))
    }).filter({ item => // Filter out terms with score too low to represent
      !item._2.isNaN
    }).toList.sortBy(_._2).reverse.take(n) // Sort by score and take the top n terms
  }

  /* Calculate total data set cardinality and memoize it for later use */
  def n: Int = {
    memo.get("cardinality") match {
      case Some(s) => s
      case None => {
        val card = redis.keys[String]("cluster:cardinality:*").get.map({ item =>
          redis.get(item.get).get.toInt
        }).sum
        memo += ("cardinality" -> card)
        card
      }
    }
  }

  /* Calculate mutual information measure between term and cluster */
  def mi(cluster: String, term: String): Double = {
    val n_11 = redis.get(s"tagcount:$cluster:$term") match {
      case Some(n) => n.toDouble
      case None => 0
    }
    val n_01 = redis.get(s"cluster:cardinality:$cluster") match {
      case Some(n) => n.toDouble - n_11
      case None => 0 - n_11
    }
    val n_10 = redis.get(s"tag:count:$term") match {
      case Some(n) => n.toDouble - n_11
      case None => 0 - n_11
    }
    val n_00 = n - n_10 - n_11 - n_01

    val t_1 = (n_11 / n) * log2(n*n_11 / ((n_11 + n_10)*(n_11 + n_01)))
    val t_2 = (n_01 / n) * log2(n*n_01 / ((n_01 + n_00)*(n_01 + n_11)))
    val t_3 = (n_10 / n) * log2(n*n_10 / ((n_11 + n_10)*(n_00 + n_10)))
    val t_4 = (n_00 / n) * log2(n*n_00 / ((n_01 + n_00)*(n_00 + n_10)))

    (t_1 + t_2 + t_3 + t_4)
  }

  /* Calculate base 2 logarithm */
  def log2(x: Double): Double = scala.math.log(x)/scala.math.log(2)
}
