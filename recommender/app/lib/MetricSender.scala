package lib

import akka.actor.{ActorSystem, Cancellable}
import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient
import com.amazonaws.services.cloudwatch.model.{MetricDatum, PutMetricDataRequest, StandardUnit}
import data.RecommenderConfiguration
import org.joda.time.DateTime
import org.joda.time.Seconds.secondsBetween

import collection.JavaConversions._
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import play.api.Logger


case class Metric(name: String, unit: StandardUnit, getValue: (ExecutionContext) => Future[Option[Double]])

class MetricSender(recommender: Recommender) {

  private val logger = Logger(classOf[MetricSender])

  private var cancel: Option[Cancellable] =  None

  private val namespace = s"${RecommenderConfiguration.stage}/recommendations"

  private val client = {
    val client = new AmazonCloudWatchClient()
    val region = Option(Regions.getCurrentRegion).getOrElse(Region.getRegion(Regions.EU_WEST_1))
    client.setRegion(region)
    client
  }

  private val metrics = List(
    Metric(
      name = "seconds_since_last_training",
      unit = StandardUnit.Seconds,
      getValue = getLastSuccessfulTrain(_)
    ),
    Metric(
      name = "elasticsearch_healthy",
      unit = StandardUnit.Count,
      getValue = getClusterHealth(_)
    )
  )

  def schedule()(implicit system: ActorSystem): Unit = {
    implicit val ec = system.dispatcher
    cancel = Some(system.scheduler.schedule(5.seconds, 59.seconds) {
      sendMetrics()
    })
  }

  def unschedule(): Unit = {
    cancel.foreach(_.cancel())
  }

  private def getLastSuccessfulTrain(implicit ec: ExecutionContext): Future[Option[Double]] = {
    recommender.getLastSuccessfulTrain() map { _.map { trainingTime =>
        secondsBetween(trainingTime, DateTime.now).getSeconds.toDouble
      }
    }
  }

  private def getClusterHealth(implicit ec: ExecutionContext): Future[Option[Double]] =
    recommender.clusterHealthy() map { health => Some(if (health) 1 else 0) }

  private def sendMetrics()(implicit ec: ExecutionContext) = {
    Future.sequence(metrics.map(createMetricRequest)).map { datums =>
      val metricData = datums.flatten
      if (metricData.nonEmpty) {
        val metricDataRequest = putMetricDataRequest(namespace, metricData)
        client.putMetricData(metricDataRequest)
      } else {
        logger.info("No metrics to send")
      }
    }
  }

  private def createMetricRequest(m: Metric)(implicit ec: ExecutionContext): Future[Option[MetricDatum]] = {
    m.getValue(ec).recover({ case _ => None }) map {
      case Some(value) =>
        Some(metricDatum(m.name, m.unit, value))
      case None =>
        logger.error(s"Failed to fetch metric ${m.name}")
        None
    }
  }

  private def putMetricDataRequest(namespace: String, metricData: List[MetricDatum]): PutMetricDataRequest = {
    val metricRequest = new PutMetricDataRequest()
    metricRequest.setNamespace(namespace)
    metricRequest.setMetricData(metricData)
    metricRequest
  }

  private def metricDatum(name: String, unit: StandardUnit, value: Double): MetricDatum = {
    val metric = new MetricDatum()
    metric.setMetricName(name)
    metric.setUnit(unit)
    metric.setValue(value)
    metric
  }
}
