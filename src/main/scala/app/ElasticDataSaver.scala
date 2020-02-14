package app

import com.typesafe.scalalogging.Logger
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

class ElasticDataSaver {

  private val LOG = Logger(getClass)

  def main(args: Array[String]) = {

    val ss = SparkSession.builder()
      .appName("spark-batching-app")
      .master("yarn")
      .getOrCreate()
    ss.sparkContext.setLogLevel("error")


    val hotelsWeather = ss.read.format("parquet").load("/tmp/201bd/dataset/hotels-weather-valid/")

    LOG.info("downloaded hotels-weather data")
    val hw = hotelsWeather.limit(300000)

    val expedia = ss.read.format("com.databricks.spark.avro").load("/tmp/201bd/dataset/expedia_valid_data/")

    val hotelsWeatherExpedia = expedia.join(hw, "hotel_id")

    val hotelsWeatherExpediaFiltered = hotelsWeatherExpedia
      .filter("avg_temp_c <= 0")
      .withColumn("duration_of_stay", datediff(hotelsWeatherExpedia("srch_co"), hotelsWeatherExpedia("srch_ci")))

    val result = hotelsWeatherExpediaFiltered
      .withColumn("stay_type", when(col("duration_of_stay") <= 0 || col("duration_of_stay") > 30, "erroneus_data")
        .when(col("duration_of_stay") === 1, "short_stay").when(col("duration_of_stay") > 2 || col("duration_of_stay") <= 7, "standart_stay")
        .when(col("duration_of_stay") > 7 || col("duration_of_stay") <= 14, "standart_extended_stay")
        .when(col("duration_of_stay") > 14 || col("duration_of_stay") <= 28, "long_stay")
        .otherwise("null")).write.parquet("/tmp/201bd/dataset/hotels_weather_expedia_valid/")
    val hotelsWetherExpediaStream = ss.readStream
      .format("parquet")
      .schema(hotelsWeatherExpediaFiltered.schema)
      .load("/tmp/201bd/dataset/hotels_weather_expedia_valid/")

    LOG.info("Starting stream for saving hotels-weather-expedia data to elasticsearch")

    //saving data to ElasticSearch
    hotelsWetherExpediaStream.writeStream
      .format("org.elasticsearch.spark.sql")
      .option("checkpointLocation", "/tmp/hwes/")
      .option("es.resource", "spark/hotels-weather-expedia").start()

  }

}
