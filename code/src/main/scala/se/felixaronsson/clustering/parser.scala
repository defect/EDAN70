import scala.io.Source._

object Parser extends Redis {

  /* Parse output from clusterdump tool and insert into redis */
  def parse_clusterdump(file: String) = {
    val cluster = """(VL-[0-9]+).*c=\[(.*)\].*""".r
    val top_terms = """^\t\t(.*)=>.*?([0-9\.]+)""".r
    val docs = """^\t1.0: (.*) = \[(.*)\]$""".r

    var current = ""

    def current_cluster = current
    def set_cluster(cluster: String) = { current = cluster }

    for (line <- fromFile(file).getLines) {
      val test = line match {
        case cluster(c, t) =>  { // Collecting cluster IDs and their terms
          redis.rpush("clusters", c)
          t.split(", ").foreach { term =>
            set_cluster(c)
            redis.sadd(s"cluster:tags:${current_cluster}", term.split(':')(0))
          }
        }
        case top_terms(term, score) => { // Getting top terms in the centroid
          redis.zadd(s"cluster:centroid:${current_cluster}", score.toDouble, term.trim)
        }
        case docs(doc, terms) => { // Parse docs and their terms
          redis.set(s"artist:cluster:$doc", current_cluster)
          terms.split(", ").foreach { t =>
            val (term, _) = t.splitAt(t.lastIndexOf(':'))
            redis.incr(s"tag:count:$term")
            redis.incr(s"tagcount:${current_cluster}:$term")
          }
          redis.incr(s"cluster:cardinality:${current_cluster}")
        }
        case _ => println("")
      }
    }
  }
}
