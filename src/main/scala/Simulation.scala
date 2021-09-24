import HelperUtils.{CreateLogger, ObtainConfigReference}
import Simulations.{BasicCloudSimPlusExample, BasicFirstExample, IaaSDatacenterSim, PaaSDatacenterSim}
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

object Simulation:
  val logger = CreateLogger(classOf[Simulation])

  @main def runSimulation =
    logger.info("Constructing a cloud model...")
    //    BasicCloudSimPlusExample.Start()
    //    BasicFirstExample.Start()
    IaaSDatacenterSim.Start()
    //    PaaSDatacenterSim.Start()
    //    SaaSDatacenterSim.Start()
    logger.info("Finished cloud simulation...")

class Simulation