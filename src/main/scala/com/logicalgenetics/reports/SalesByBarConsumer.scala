package com.logicalgenetics.reports

import java.lang.Double
import java.time.Duration
import java.util

import io.confluent.kafka.serializers.KafkaAvroDeserializer
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer

import scala.collection.JavaConverters._

object SalesByBarConsumer {

  val topic = "beers"

  lazy val consumer: KafkaConsumer[String, GenericRecord] = {
    val properties = new util.Properties()
    properties.put("bootstrap.servers", "localhost:9092")
    properties.put("schema.registry.url", "http://localhost:8081")
    properties.put("group.id", "cheese-group")
    properties.put("key.deserializer", classOf[StringDeserializer])
    properties.put("value.deserializer", classOf[KafkaAvroDeserializer])
    properties.put("auto.offset.reset", "earliest")

    new KafkaConsumer[String, GenericRecord](properties)
  }

  def main(args: Array[String]): Unit = {
    consumer.subscribe(util.Arrays.asList(topic))

    var i = 0
    while (true) {
      val records = consumer.poll(Duration.ofSeconds(5))
      println(s"Read ${records.count()}")

      for (record <- records.iterator().asScala) {
        val beer = record.value()
        val name = beer.get("name").toString
        val abv = beer.get("abv") match {
          case null => "unknown"
          case x : Double => s"${x * 100}%"
        }
        println(s"$name $abv")
      }
    }
  }
}