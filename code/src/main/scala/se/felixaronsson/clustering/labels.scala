case class Config(mode: String = "", num_labels: Integer = 10,
                  file: String = "", cluster: String = "",
                  artist: String = "")

object Labels extends Redis {

  def main(args: Array[String]) = {
    val parser = new scopt.OptionParser[Config]("labels") {
      head("labels", "0.1")
      cmd("parse") action { (_, c) =>
        c.copy(mode = "parse") } text("Parse clusterdump file.") children(
        opt[String]('f', "file") valueName("<file>") action { (f, c) =>
          c.copy(file = f) } text("path to clusterdump file")
      )

      cmd("labels") action { (_, c) =>
        c.copy(mode = "labels") } text("Compute cluster labels.") children(
        opt[Int]('n', "num_labels") action { (n, c) =>
          c.copy(num_labels = n) } text("number of labels to print"),
        opt[String]('c', "cluster") action { (cl, c) =>
          c.copy(cluster = cl) } text("cluster to calculate labels for"),
        opt[String]('a', "artist") action { (a, c) =>
          c.copy(artist = a) } text("calculate labels for cluster which includes artist")
      )

      checkConfig { c =>
        if (c.mode == "parse" && c.file == "") failure("please specify a file to read") else success
        if (c.mode == "labels" && (c.artist != "" && c.cluster != "")) failure("cluster and artist can not be specified at the same time") else success
      }

      help("help") text("prints this usage text")
    }

    parser.parse(args, Config()) match {
      case Some(config) => {
        config.mode match {
          case "parse" => {
            Parser.parse_clusterdump(config.file)
          }
          case "labels" => {
            if (config.cluster != "") { // get labels to specific cluster
              print_clusters(List(config.cluster), config.num_labels)
            } else if (config.artist != "") { // get labels to cluster containing specific artist
              redis.get(s"artist:cluster:${config.artist}") match {
                case Some(key) => print_clusters(List(key), config.num_labels)
                case None => println("No such artist")
              }
            } else { // get labels to all clusters
              redis.lrange("clusters", 0, -1) match {
                case Some(result) => print_clusters(result.map({ cluster =>
                  cluster.get
                }), config.num_labels)
                case None => println("Could not retrieve clusters")
              }
            }
          }
        }
      }
      case None => {}
    }
  }

  /* Print a list of clusters and with `num_labels` labels */
  def print_clusters(clusters: List[String], num_labels: Int = 3) = {
    for (cluster <- clusters) {
      print_labels(cluster, MutualInformation.labels(cluster, num_labels), Centroid.labels(cluster, num_labels))
    }
  }

  /* Print the labels of a cluster in a somewhat structured manner */
  def print_labels(cluster: String, mi_labels: List[Tuple2[String, Double]], centroid_labels: List[Tuple2[String, Double]]) = {
    val mi_max_len = mi_labels.maxBy({ item =>
      item._1.size
    })._1.size
    val centroid_max_len = centroid_labels.maxBy({ item =>
      item._1.size
    })._1.size

    println(cluster)
    println("-" * (mi_max_len + centroid_max_len + 3))
    for (i <- centroid_labels.indices)
      println(s"${mi_labels(i)._1.padTo(mi_max_len, ' ')} | ${centroid_labels(i)._1}")
    println("-" * (mi_max_len + centroid_max_len + 3))
  }

}

