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

import scala.collection.JavaConverters.*

/**
 * This Simulation is done for showing the functionality of Platform as a Service.
 *
 * All parameters are specified in the simulationPaaS.conf file.
 *
 * The code entry point is the runSimulation method in Simulation.scala.
 *
 * @author Vivek Mishra
 *
 */

class PaaSDatacenterSim

object PaaSDatacenterSim {
  val config: Config = ConfigFactory.load("simulationPaaS" + ".conf")
  val logger = CreateLogger(classOf[PaaSDatacenterSim])
  val cloudsim = new CloudSim();

  val datacenterPaaS = createPaaSDatacenter();
  val broker0 = new DatacenterBrokerSimple(cloudsim);

  def Start() = {
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

  /**
   *
   * This method is used to create Datacenters, the configuration for the datacenter is loaded from
   * the config file.
   * Furthermore, this method relies on createHost in order to create Host nodes inside a datacenter.
   *
   * @return Created Datacenter instance.
   */
  def createPaaSDatacenter(): Datacenter = {
    // Get all the datacenter config details
    val hostRam = config.getLong("cloudSimulator.dc.host.HOST_RAM");
    val hostStorage = config.getLong("cloudSimulator.dc.host.HOST_STORAGE");
    val hostBW = config.getLong("cloudSimulator.dc.host.HOST_BW");
    val hostMIPS = config.getLong("cloudSimulator.dc.host.HOST_MIPS");
    val hostPes = config.getInt("cloudSimulator.dc.host.HOST_PES");
    val costPerMem = config.getDouble("cloudSimulator.dc.costPerMem");
    val costPerStorage = config.getDouble("cloudSimulator.dc.costPerStorage");
    val costPerBw = config.getDouble("cloudSimulator.dc.costPerBw");
    val costPerSec = config.getDouble("cloudSimulator.dc.costPerSec");
    val numOfHosts = config.getInt("cloudSimulator.dc.numHosts")

    val hostList: List[Host] = createHost(hostPes, hostMIPS, numOfHosts, hostRam, hostStorage, hostBW)

    val datacenter: Datacenter = new DatacenterSimple(cloudsim, hostList.asJava);

    datacenter.getCharacteristics()
      .setCostPerSecond(costPerSec)
      .setCostPerBw(costPerBw)
      .setCostPerMem(costPerMem)
      .setCostPerStorage(costPerStorage)

    return datacenter;
  }

  /**
   * Simulate host creation by providing different inputs
   *
   * @param hostPes     specifies host processing elements
   * @param hostMIPS    specifies host processing speed in Million Instuctions per sec
   * @param numOfHosts  Specifies number of hosts in the datacenter
   * @param hostRam     specifies host Ram in Mbs
   * @param hostStorage specifies host storage
   * @param hostBW      specifies host's bandwidth
   * @return hostList         Returns List[Host]
   */
  def createHost(hostPes: Int, hostMIPS: Long, numOfHosts: Int,
                 hostRam: Long, hostStorage: Long, hostBW: Long): List[Host] = {

    // A Machine contains one or more PEs or CPUs/Cores. We iterate over the number of host Processing elements
    val pesList = (1 to hostPes).map(pl => new PeSimple(hostMIPS, new PeProvisionerSimple()));

    //
    val hostList = (1 to numOfHosts).map(hl =>
      new HostSimple(hostRam, hostStorage, hostBW, pesList.asJava)
        .setRamProvisioner(new ResourceProvisionerSimple())
        .setBwProvisioner(new ResourceProvisionerSimple())
        .setVmScheduler(new VmSchedulerTimeShared())
    ).toList

    logger.info(s"Created one processing element: $hostPes")
    logger.info(s"Created one host: $hostList")

    return hostList;
  }

  /**
   * Simulate the IaaS scenarios by providing the different Configurations for Vms
   *
   * cloudletScheduler  for scheduling cloudlet to Vms
   *
   * @return retruns List[Vm]
   */
  def createVms(): List[Vm] = {
    val hostMIPS: Long = config.getLong("cloudSimulator.vm.VM_MIPS");
    val vmPes: Long = config.getLong("cloudSimulator.vm.VM_PES");
    val vmRam: Long = config.getLong("cloudSimulator.vm.VM_RAM");
    val vmBW: Long = config.getLong("cloudSimulator.vm.VM_BW");
    val vmSize: Long = config.getLong("cloudSimulator.vm.VM_SIZE");
    val vmNum: Int = config.getInt("cloudSimulator.vm.VMS");

    val vmList = (1 to vmNum).map(vm => new VmSimple(hostMIPS, vmPes)
      .setSize(vmSize).setBw(vmBW).setRam(vmRam)).toList

    logger.info(s"Created one virtual machine: $vmList")
    return vmList.toList;
  }

  /**
   * Simulate the IaaS scenarios by providing the different Configurations for couldlets
   *
   * utilizationModel   utilization model for RAM,BW and storage of cloudlets
   *
   * @return List[Cloudlet]
   */
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

    println(f"Total cost ($$) for ${totalNonIdleVms.asInstanceOf[Int]}%3d created VMs from " +
      f"${broker0.getVmsNumber}%3d in DC ${datacenterPaaS.getId}%d: ${processingTotalCost}%8.2f$$ " +
      f"${memoryTotaCost}%13.2f$$ ${storageTotalCost}%17.2f$$ ${bwTotalCost}%12.2f$$ ${totalCost}%15.2f$$")
  }
}