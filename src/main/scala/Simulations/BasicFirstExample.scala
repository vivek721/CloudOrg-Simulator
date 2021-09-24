package Simulations

import org.cloudbus.cloudsim.brokers.DatacenterBroker
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple
import org.cloudbus.cloudsim.cloudlets.Cloudlet
import org.cloudbus.cloudsim.cloudlets.CloudletSimple
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.datacenters.Datacenter
import org.cloudbus.cloudsim.datacenters.DatacenterSimple
import org.cloudbus.cloudsim.hosts.Host
import org.cloudbus.cloudsim.hosts.HostSimple
import org.cloudbus.cloudsim.resources.Pe
import org.cloudbus.cloudsim.resources.PeSimple
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelDynamic
import org.cloudbus.cloudsim.vms.Vm
import org.cloudbus.cloudsim.vms.VmSimple
import org.cloudsimplus.builders.tables.CloudletsTableBuilder
import HelperUtils.{CreateLogger, ObtainConfigReference}

import collection.JavaConverters.*

class BasicFirstExample

object BasicFirstExample {
  val config = ObtainConfigReference("cloudSimulator1") match {
    case Some(value) => value
    case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
  }

  val logger = CreateLogger(classOf[BasicFirstExample])

  def Start() = {
    val cloudsim = new CloudSim();
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
    val hostRam = config.getLong("cloudSimulator1.host.HOST_RAM");
    val hostStorage = config.getLong("cloudSimulator1.host.HOST_STORAGE");
    val hostBW = config.getLong("cloudSimulator1.host.HOST_BW");
    val hostMIPS = config.getLong("cloudSimulator1.host.HOST_MIPS");
    val hostPes = config.getInt("cloudSimulator1.host.HOST_PES");

    val pesList = (1 to hostPes).map(pl => new PeSimple(hostMIPS));

    val hostList = (1 to config.getInt("cloudSimulator1.host.HOSTS")).map(hl =>
      new HostSimple(hostRam, hostStorage, hostBW, pesList.asJava)).toList

    logger.info(s"Created one processing element: $hostPes")
    logger.info(s"Created one host: $hostList")
    return new DatacenterSimple(cloudsim, hostList.asJava);
  }

  def createVms(): List[Vm] = {
    val hostMIPS: Long = config.getLong("cloudSimulator1.host.HOST_MIPS");
    val vmPes: Long = config.getLong("cloudSimulator1.vm.VM_PES");
    val vmRam: Long = config.getLong("cloudSimulator1.vm.VM_RAM");
    val vmBW: Long = config.getLong("cloudSimulator1.vm.VM_BW");
    val vmSize: Long = config.getLong("cloudSimulator1.vm.VM_SIZE");
    val vmNum: Int = config.getInt("cloudSimulator1.vm.VMS");

    val vmList = (1 to vmNum).map(vm => new VmSimple(hostMIPS, vmPes).setSize(vmSize).setBw(vmBW).setRam(vmRam)).toList
    logger.info(s"Created one virtual machine: $vmList")
    return vmList.toList;
  }

  def createCloudlets(): List[Cloudlet] = {
    val utilizationModel = new UtilizationModelDynamic(config.getDouble("cloudSimulator1.UTILIZATIONRATIO"));

    val cloudlet = (1 to config.getInt("cloudSimulator1.cloudlet.CLOUDLETS")).map(cl =>
      new CloudletSimple(config.getLong("cloudSimulator1.cloudlet.CLOUDLET_LENGTH"),
        config.getInt("cloudSimulator1.cloudlet.CLOUDLET_PES"), utilizationModel)).toList
    logger.info(s"Created a list of cloudlets1: $cloudlet")

    return cloudlet.toList;
  }

}
