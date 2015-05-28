import com.redis.RedisClient.DESC

object Centroid extends Redis {

  /* Get top n labels based on the cluster centroid */
  def labels(cluster: String, n: Int): List[Tuple2[String, Double]] = {
    redis.zrangeWithScore[String](s"cluster:centroid:$cluster", 0, (n-1), DESC) match {
      case Some(list) => list
      case None => List()
    }
  }
}
