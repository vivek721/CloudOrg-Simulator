package Simulations

import HelperUtils.{CreateLogger, ObtainConfigReference}
import com.typesafe.config.{Config, ConfigFactory}
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicySimple
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple
import org.cloudbus.cloudsim.cloudlets.{Cloudlet, CloudletSimple}
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.datacenters.{Datacenter, DatacenterSimple}
import org.cloudbus.cloudsim.hosts.HostSimple
import org.cloudbus.cloudsim.network.topologies.{BriteNetworkTopology, NetworkTopology}
import org.cloudbus.cloudsim.provisioners.{PeProvisioner, PeProvisionerSimple, ResourceProvisionerSimple}
import org.cloudbus.cloudsim.resources.PeSimple
import org.cloudbus.cloudsim.schedulers.cloudlet.{CloudletSchedulerSpaceShared, CloudletSchedulerTimeShared}
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerTimeShared
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelDynamic
import org.cloudbus.cloudsim.vms.{Vm, VmCost, VmSimple}
import org.cloudsimplus.builders.tables.CloudletsTableBuilder

import collection.JavaConverters.*

class IaaSDatacenterSim

object IaaSDatacenterSim {
  val config: Config = ConfigFactory.load("simulationIaaS" + ".conf")
  val logger = CreateLogger(classOf[IaaSDatacenterSim])
  val cloudsim = new CloudSim();

  val datacenterIaaS = createIaaSDatacenter();

  // Creates a broker that is a software acting on behalf a cloud customer to manage his/her VMs and Cloudlets

  val broker0 = new DatacenterBrokerSimple(cloudsim);

  def Start() = {
    configureNetwork();

    // Create VM list
    val vmList = createVms();

    //create cloudlets
    val cloudletList = createCloudlets();

    broker0.submitVmList(vmList.asJava);
    broker0.submitCloudletList(cloudletList.asJava);
    logger.info("Starting cloud simulation...")

    cloudsim.start();

    new CloudletsTableBuilder(broker0.getCloudletFinishedList()).build();
    printTotalVmsCost();
  }

  def configureNetwork(): Unit = {
    val networkBW: Double = config.getDouble("cloudSimulator.datacenter.NETWORK_BW");
    val networkLatency: Double = config.getDouble("cloudSimulator.datacenter.NETWORK_LATENCY");

    //Configure network by mapping CloudSim entities to BRITE entities
    val networkTopology: NetworkTopology = new BriteNetworkTopology();
    cloudsim.setNetworkTopology(networkTopology);
    networkTopology.addLink(datacenterIaaS, broker0, networkBW, networkLatency);
  }

  def createIaaSDatacenter(): Datacenter = {
    // Get all the datacenter config details
    val hostRam = config.getLong("cloudSimulator.datacenter.host.HOST_RAM");
    val hostStorage = config.getLong("cloudSimulator.datacenter.host.HOST_STORAGE");
    val hostBW = config.getLong("cloudSimulator.datacenter.host.HOST_BW");
    val hostMIPS = config.getLong("cloudSimulator.datacenter.host.HOST_MIPS");
    val hostPes = config.getInt("cloudSimulator.datacenter.host.HOST_PES");
    val costPerMem = config.getDouble("cloudSimulator.datacenter.costPerMem");
    val costPerStorage = config.getDouble("cloudSimulator.datacenter.costPerStorage");
    val costPerBw = config.getDouble("cloudSimulator.datacenter.costPerBw");
    val costPerSec = config.getDouble("cloudSimulator.datacenter.costPerSec");
    val schedulingInterval = config.getInt("cloudSimulator.datacenter.SCHEDULING_INTERVAL");
    val arch = config.getString("cloudSimulator.datacenter.arch");
    val os = config.getString("cloudSimulator.datacenter.os");
    val vmm = config.getString("cloudSimulator.datacenter.vmm");

    // A Machine contains one or more PEs or CPUs/Cores. We iterate over the number of host Processing elements
    val pesList = (1 to hostPes).map(pl => new PeSimple(hostMIPS, new PeProvisionerSimple()));

    //
    val hostList = (1 to config.getInt("cloudSimulator.datacenter.numHosts")).map(hl =>
      new HostSimple(hostRam, hostStorage, hostBW, pesList.asJava)
        .setRamProvisioner(new ResourceProvisionerSimple())
        .setBwProvisioner(new ResourceProvisionerSimple())
        .setVmScheduler(new VmSchedulerTimeShared())
    ).toList

    val datacenter: Datacenter = new DatacenterSimple(cloudsim, hostList.asJava)
      .setSchedulingInterval(schedulingInterval)

    datacenter.getCharacteristics()
      .setArchitecture(arch)
      .setOs(os)
      .setVmm(vmm)
      .setCostPerSecond(costPerSec)
      .setCostPerBw(costPerBw)
      .setCostPerMem(costPerMem)
      .setCostPerStorage(costPerStorage)

    logger.info(s"Created one processing element: $hostPes")
    logger.info(s"Created one host: $hostList")
    return datacenter;
  }


  def createVms(): List[Vm] = {
    val hostMIPS: Long = config.getLong("cloudSimulator.vm.VM_MIPS");
    val vmPes: Long = config.getLong("cloudSimulator.vm.VM_PES");
    val vmRam: Long = config.getLong("cloudSimulator.vm.VM_RAM");
    val vmBW: Long = config.getLong("cloudSimulator.vm.VM_BW");
    val vmSize: Long = config.getLong("cloudSimulator.vm.VM_SIZE");
    val vmNum: Int = config.getInt("cloudSimulator.vm.VMS");

    val vmList = (1 to vmNum).map(vm => new VmSimple(hostMIPS, vmPes)
      .setSize(vmSize).setBw(vmBW).setRam(vmRam)
      .setCloudletScheduler(new CloudletSchedulerTimeShared())).toList

    logger.info(s"Created one virtual machine: $vmList")
    return vmList.toList;
  }

  def createCloudlets(): List[Cloudlet] = {
    val utilizationRatio: Double = config.getDouble("cloudSimulator.UTILIZATIONRATIO")
    val numOfCloudlet: Int = config.getInt("cloudSimulator.cloudlet.CLOUDLETS")
    val cloudletLength: Long = config.getLong("cloudSimulator.cloudlet.CLOUDLET_LENGTH")
    val cloudletPes: Int = config.getInt("cloudSimulator.cloudlet.CLOUDLET_PES")

    val utilizationModel = new UtilizationModelDynamic(utilizationRatio);

    val cloudlet = (1 to numOfCloudlet).map(cl =>
      new CloudletSimple(cloudletLength,
        cloudletPes, utilizationModel)).toList
    logger.info(s"Created a list of cloudlets1: $cloudlet")

    return cloudlet.toList;
  }

  /**
   * Computes and print the cost ($) of resources (processing, bw, memory, storage)
   * for each VM inside the datacenter.
   */
  def printTotalVmsCost(): Unit = {
    var totalCost: Double = 0.0
    var processingTotalCost: Double = 0.0
    var totalNonIdleVms: Double = 0.0
    var memoryTotaCost: Double = 0.0
    var storageTotalCost: Double = 0.0
    var bwTotalCost: Double = 0.0

    val vmList: List[Vm] = broker0.getVmCreatedList().asScala.toList

    vmList.map((vm) => {
      val cost: VmCost = new VmCost(vm);
      processingTotalCost += cost.getProcessingCost()
      memoryTotaCost += cost.getMemoryCost()
      storageTotalCost += cost.getStorageCost()
      bwTotalCost += cost.getBwCost()
      totalCost += cost.getTotalCost()
      totalNonIdleVms = totalNonIdleVms + {
        if (vm.getTotalExecutionTime > 0) 1 else 0
      }
      println(cost)
    });

    println(f"Total cost ($$) for ${totalNonIdleVms.asInstanceOf[Int]}%3d created VMs from ${broker0.getVmsNumber}%3d in DC" +
      f" ${datacenterIaaS.getId}%d: ${processingTotalCost}%8.2f$$ ${memoryTotaCost}%13.2f$$" +
      f" ${storageTotalCost}%17.2f$$ ${bwTotalCost}%12.2f$$ ${totalCost}%15.2f$$")
  }


}

