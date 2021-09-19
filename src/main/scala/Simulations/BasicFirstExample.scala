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
  val UTILIZATIONRATIO: Float = 0.5;
  val HOSTS: Int = 1;
  val HOST_PES: Int = 8;
  val HOST_MIPS: Int = 1000;
  val HOST_RAM: Int = 2048;
  val HOST_BW: Int = 10000;
  val HOST_STORAGE: Int = 1000000;

  val VMS: Int = 2;
  val VM_PES: Int = 1;
  val VM_RAM: Int = 512;
  val VM_BW: Int = 1000;
  val VM_SIZE: Int = 10000;

  val CLOUDLETS: Int = 4;
  val CLOUDLET_PES: Int = 2;
  val CLOUDLET_LENGTH: Int = 10000;

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
    cloudsim.start();

    new CloudletsTableBuilder(broker0.getCloudletFinishedList()).build();
  }

  def createDatacenter(cloudsim: CloudSim): Datacenter = {
    val hostPes = List(new PeSimple(HOST_MIPS));
    val hostList = List(new HostSimple(HOST_RAM, HOST_STORAGE, HOST_BW, hostPes.asJava))
    return new DatacenterSimple(cloudsim, hostList.asJava);
  }

  def createVms(): List[Vm] = {
    val vmList = List(new VmSimple(HOST_MIPS, VM_PES)
      .setRam(VM_RAM).setSize(VM_SIZE).setBw(VM_BW))
    return vmList;
  }

  def createCloudlets(): List[Cloudlet] = {
    val utilizationModel = new UtilizationModelDynamic(UTILIZATIONRATIO);
    val cloudlet = new CloudletSimple(CLOUDLET_LENGTH, CLOUDLET_PES, utilizationModel) ::
      new CloudletSimple(CLOUDLET_LENGTH, CLOUDLET_PES, utilizationModel) :: Nil;
    return cloudlet;
  }

}
