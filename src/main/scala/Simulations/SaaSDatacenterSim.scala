package Simulations

import HelperUtils.{CreateLogger, ObtainConfigReference}
import com.typesafe.config.{Config, ConfigFactory}
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicySimple
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple
import org.cloudbus.cloudsim.cloudlets.{Cloudlet, CloudletSimple}
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.datacenters.{Datacenter, DatacenterSimple}
import org.cloudbus.cloudsim.hosts.{Host, HostSimple}
import org.cloudbus.cloudsim.network.topologies.{BriteNetworkTopology, NetworkTopology}
import org.cloudbus.cloudsim.provisioners.{PeProvisioner, PeProvisionerSimple, ResourceProvisionerSimple}
import org.cloudbus.cloudsim.resources.PeSimple
import org.cloudbus.cloudsim.schedulers.cloudlet.{CloudletSchedulerSpaceShared, CloudletSchedulerTimeShared}
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerTimeShared
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelDynamic
import org.cloudbus.cloudsim.vms.{Vm, VmCost, VmSimple}
import org.cloudsimplus.builders.tables.CloudletsTableBuilder
import collection.JavaConverters.*

class SaaSDatacenterSim

object SaaSDatacenterSim{
  val config: Config = ConfigFactory.load("simulationSaaS" + ".conf")
  val logger = CreateLogger(classOf[PaaSDatacenterSim])
  val cloudsim = new CloudSim();

  def Start() = {
    val datacenter0 = createDatacenter(cloudsim);

    //Creates a broker that is a software acting on behalf a cloud customer to manage his/her VMs and Cloudlets
    val broker0 = new DatacenterBrokerSimple(cloudsim);

    // Create VM list
    val vmList = createVms();

    //create cloudlets
    val cloudletList = createCloudlets();

    broker0.submitVmList(vmList.asJava);
    broker0.submitCloudletList(cloudletList.asJava);
    logger.info("Starting cloud simulation...")

    cloudsim.start();

    new CloudletsTableBuilder(broker0.getCloudletFinishedList()).build();
  }

  def createDatacenter(cloudsim: CloudSim): Datacenter = {
    val hostRam = config.getLong("cloudSimulator.host.HOST_RAM");
    val hostStorage = config.getLong("cloudSimulator.host.HOST_STORAGE");
    val hostBW = config.getLong("cloudSimulator.host.HOST_BW");
    val hostMIPS = config.getLong("cloudSimulator.host.HOST_MIPS");
    val hostPes = config.getInt("cloudSimulator.host.HOST_PES");

    val pesList = (1 to hostPes).map(pl => new PeSimple(hostMIPS));

    val hostList = (1 to config.getInt("cloudSimulator.host.HOSTS")).map(hl =>
      new HostSimple(hostRam, hostStorage, hostBW, pesList.asJava)).toList

    logger.info(s"Created one processing element: $hostPes")
    logger.info(s"Created one host: $hostList")
    return new DatacenterSimple(cloudsim, hostList.asJava);
  }

  def createVms(): List[Vm] = {
    val hostMIPS: Long = config.getLong("cloudSimulator.host.HOST_MIPS");
    val vmPes: Long = config.getLong("cloudSimulator.vm.VM_PES");
    val vmRam: Long = config.getLong("cloudSimulator.vm.VM_RAM");
    val vmBW: Long = config.getLong("cloudSimulator.vm.VM_BW");
    val vmSize: Long = config.getLong("cloudSimulator.vm.VM_SIZE");
    val vmNum: Int = config.getInt("cloudSimulator.vm.VMS");

    val vmList = (1 to vmNum).map(vm => new VmSimple(hostMIPS, vmPes).setSize(vmSize).setBw(vmBW).setRam(vmRam)).toList
    logger.info(s"Created one virtual machine: $vmList")
    return vmList.toList;
  }

  def createCloudlets(): List[Cloudlet] = {
    val utilizationModel = new UtilizationModelDynamic(config.getDouble("cloudSimulator.UTILIZATIONRATIO"));

    val cloudlet = (1 to config.getInt("cloudSimulator.cloudlet.CLOUDLETS")).map(cl =>
      new CloudletSimple(config.getLong("cloudSimulator.cloudlet.CLOUDLET_LENGTH"),
        config.getInt("cloudSimulator.cloudlet.CLOUDLET_PES"), utilizationModel)).toList
    logger.info(s"Created a list of cloudlets1: $cloudlet")

    return cloudlet.toList;
  }
}