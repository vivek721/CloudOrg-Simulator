package Simulations

import Simulations.IaaSDatacenterSim.{createHost, createIaaSDatacenter}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class IaaSDatacenterSimTestSuite extends AnyFlatSpec with Matchers{
  behavior of "Datacenter function"

  it should "Datacenter getId should return an id" in {
    createIaaSDatacenter("dc1").getId should be > -1L
  }

  it should "hostList should be length of pesList" in {

  }
}
