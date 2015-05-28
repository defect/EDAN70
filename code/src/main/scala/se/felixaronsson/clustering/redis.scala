import com.redis.RedisClient

trait Redis {
  val host = "localhost"
  val port = 6379
  val redis = new RedisClient(host, port)
}
