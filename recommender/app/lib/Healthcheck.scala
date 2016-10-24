package lib

import akka.actor.{ActorSystem, Cancellable}
import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient
import com.amazonaws.services.cloudwatch.model.{MetricDatum, PutMetricDataRequest, StandardUnit}
import data.RecommenderConfiguration
import org.joda.time.{DateTime, Seconds}

import collection.JavaConversions._
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import play.api.Logger

class Healthcheck(recommender: Recommender) {

  private val logger = Logger(classOf[Healthcheck])

  private var cancel: Option[Cancellable] =  None

  private val client = {
    val client = new AmazonCloudWatchClient()
    val region = Option(Regions.getCurrentRegion).getOrElse(Region.getRegion(Regions.EU_WEST_1))
    client.setRegion(region)
    client
  }

  def reportTimeSinceSuccessfulTrain()(implicit ec: ExecutionContext): Unit = {
    recommender.getLastSuccessfulTrain() map {
      case Some(trainingTime) =>
        val metricRequest = new PutMetricDataRequest()
        metricRequest.setNamespace(s"${RecommenderConfiguration.stage}/recommendations")
        val timeSinceLastTraining = Seconds.secondsBetween(trainingTime, DateTime.now)
        metricRequest.setMetricData(
          List(metric(
            name = "seconds_since_last_training",
            unit = StandardUnit.Seconds,
            value = timeSinceLastTraining.getSeconds.toDouble
          ))
        )
        client.putMetricData(metricRequest)
      case None =>
        logger.error("Failed to fetch last sucessful training time")
    }
  }

  def schedule()(implicit system: ActorSystem): Unit = {
    implicit val ec = system.dispatcher
    cancel = Some(system.scheduler.schedule(5.seconds, 5.minutes) {
      reportTimeSinceSuccessfulTrain()
    })
  }

  def unschedule(): Unit = {
    cancel.foreach(_.cancel())
  }

  private def metric(name: String, unit: StandardUnit, value: Double) = {
    val metric = new MetricDatum()
    metric.setMetricName(name)
    metric.setUnit(unit)
    metric.setValue(value)
    metric
  }
}
