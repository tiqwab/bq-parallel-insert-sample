package com.tiqwab.example.bq

import java.io.FileInputStream

import com.google.auth.oauth2.ServiceAccountCredentials

import scala.collection.JavaConverters._
import scala.concurrent.blocking
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits._
import com.google.cloud.bigquery._
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.Future

object Main {

  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load()

    val credentialsPath = config.getString("bq.credential.path")
    implicit val bq = BigQueryOptions.newBuilder()
      .setCredentials(ServiceAccountCredentials.fromStream(new FileInputStream(credentialsPath)))
      .build()
      .getService()

    val dataSetName = "streaming_insert_sample"

    println(s"start ${System.currentTimeMillis()}")

    val f = Future.traverse(1.to(10)) { x =>
      val tableId = TableId.of(dataSetName, s"ten_sec_limit_sample_$x")
      insertSimpleRecord(tableId)
    }

    val responses = Await.result(f, 1.minute)
    val errorResponses = responses.filter(_.hasErrors)
    if (errorResponses.size > 0) {
      println("there are some errors")
      errorResponses foreach { println(_)}
    } else {
      println("success")
    }
    responses filter(_.hasErrors) foreach(println(_))

    println(s"end ${System.currentTimeMillis()}")
  }

  def createSimpleRecord: Map[String, Any] = {
    Map("appId" -> "4", "uuid" -> "4")
  }

  def insertSimpleRecord(tableId: TableId)(implicit bq: BigQuery): Future[InsertAllResponse] = {
    val insertReq = InsertAllRequest.newBuilder(tableId)
      .addRow(createSimpleRecord.asJava)
      .build()
    Future {
      blocking {
        bq.insertAll(insertReq)
      }
    }
  }

}
