package lib

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.regions.{Regions, Region}
import com.amazonaws.services.ec2.AmazonEC2Client
import com.amazonaws.services.ec2.model.{InstanceStateName, Filter, DescribeInstancesRequest}
import play.Logger
import scala.collection.JavaConversions._

trait ElasticSearchDiscovery {
  def app: String

  def stack: String

  def stage: String

  def region: String

  def findElasticsearchHostsInEc2: List[String] = {
    val client = new AmazonEC2Client(new DefaultAWSCredentialsProviderChain)
    client.setRegion(Region.getRegion(Regions.fromName(region)))

    Logger.info(s"Searching $app $stage hosts in EC2...")

    val possibleHosts = client.describeInstances(
      new DescribeInstancesRequest().withFilters(
        new Filter("instance-state-name", List(InstanceStateName.Running.toString)),
        new Filter("tag:Stage", List(stage)),
        new Filter("tag:App", List(app)),
        new Filter("tag:Stack", List(stack))
      )
    )
    val possibleHostList = possibleHosts.getReservations.flatMap(_.getInstances).map(_.getPublicDnsName).toList
    Logger.info(s"Found possible $stack Elasticsearch hosts in EC2: [$possibleHostList]")
    if (possibleHostList.isEmpty) Logger.error("Unable to find any Elasticsearch hosts in EC2")
    possibleHostList
  }
}
