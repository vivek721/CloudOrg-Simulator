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
    val hostPes = List(new PeSimple(config.getLong("cloudSimulator1.host.HOST_MIPS")));
    val hostList = List(new HostSimple(config.getLong("cloudSimulator1.host.HOST_RAM"),
      config.getLong("cloudSimulator1.host.HOST_STORAGE"),
      config.getLong("cloudSimulator1.host.HOST_BW"), hostPes.asJava))

    logger.info(s"Created one processing element: $hostPes")
    logger.info(s"Created one host: $hostList")
    return new DatacenterSimple(cloudsim, hostList.asJava);
  }

  def createVms(): List[Vm] = {
    val vmList = List(new VmSimple(config.getLong("cloudSimulator1.host.HOST_MIPS")
      , (config.getLong("cloudSimulator1.vm.VM_PES")))
      .setRam(config.getLong("cloudSimulator1.vm.VM_RAM"))
      .setSize(config.getLong("cloudSimulator1.vm.VM_SIZE"))
      .setBw(config.getLong("cloudSimulator1.vm.VM_BW")))
    logger.info(s"Created one virtual machine: $vmList")
    return vmList;
  }

  def createCloudlets(): List[Cloudlet] = {
    val utilizationModel = new UtilizationModelDynamic(config.getDouble("cloudSimulator1.UTILIZATIONRATIO"));
    val cloudlet = new CloudletSimple(config.getLong("cloudSimulator1.cloudlet.CLOUDLET_LENGTH"),
      config.getInt("cloudSimulator1.cloudlet.CLOUDLET_PES"), utilizationModel) ::
      new CloudletSimple(config.getLong("cloudSimulator1.cloudlet.CLOUDLET_LENGTH"),
        config.getInt("cloudSimulator1.cloudlet.CLOUDLET_PES"), utilizationModel) :: Nil;

    logger.info(s"Created a list of cloudlets: $cloudlet")
    return cloudlet;
  }

}
