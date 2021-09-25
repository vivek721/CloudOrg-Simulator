package Simulations

import Simulations.IaaSDatacenterSim.{createHost, createIaaSDatacenter}
import Simulations.SaaSDatacenterSim.createDatacenter
import com.typesafe.config.{Config, ConfigFactory}
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicySimple
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.brokers.{DatacenterBrokerFirstFit, DatacenterBrokerSimple}
import org.cloudbus.cloudsim.datacenters.Datacenter
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class SimulationTestSuite extends AnyFlatSpec with Matchers {

  val cloudsim: CloudSim = new CloudSim()
  val broker0: DatacenterBrokerSimple = new DatacenterBrokerSimple(cloudsim)

  // Datacenter Id should be greater than 1
  it should "Datacenter getId should return an id" in {
    createIaaSDatacenter("dc1").getId should be > -1L
    createDatacenter(cloudsim).getId should be > -1L
  }

  val configIaaS: Config = ConfigFactory.load("simulationIaaS" + ".conf")
  val configPaaS: Config = ConfigFactory.load("simulationPaaS" + ".conf")
  val configSaaS: Config = ConfigFactory.load("simulationSaaS" + ".conf")

  // Test what service is encoded for simulation2 in application.conf
  it should "OS should be of type Linux or Ununtu" in {
    val os1 = configIaaS.getString("cloudSimulator.dc1.os")
    val os2 = configIaaS.getString("cloudSimulator.dc2.os")
    assert(os1 == "Linux" && os2 == "Ubuntu")
  }

  // Type of VM allocation policy should be of the same as the integer passed
  it should "passing 1 it should be of type VmAllocationPolicySimple" in {
    val vmAllocationPolicy = IaaSDatacenterSim.selectVmAllocationPolicy(1)
    assert(vmAllocationPolicy.isInstanceOf[VmAllocationPolicySimple])
  }

  // Type of broker policy should be of the same as the integer passed
  it should "broker policy should return DatacenterBrokerFirstFit type" in {
    val brokerPolicy = IaaSDatacenterSim.createBrokerWithSelectedPolicy()
    assert(brokerPolicy.isInstanceOf[DatacenterBrokerFirstFit])
  }

  // number of VM's created should be same as config file
  it should "Number of VM should be same as defined in config" in {
    val Vms = configSaaS.getInt("cloudSimulator.vm.VMS")
    val vmlist = SaaSDatacenterSim.createVms()
    assert(vmlist.length == Vms)
  }

  // number of clodlets's created should be same as config file
  it should "Number of clodlets should be same as defined in config" in {
    val clodlets = configSaaS.getInt("cloudSimulator.cloudlet.CLOUDLETS")
    val cloudletList = SaaSDatacenterSim.createCloudlets()
    assert(cloudletList.length == clodlets)
  }

  // return type of createDatacenter should be Datacenter
  it should "return type of createDatacenter should be Datacenter" in {
    val dataCenter = SaaSDatacenterSim.createDatacenter(cloudsim)
    assert(dataCenter.isInstanceOf[Datacenter])
  }
}
