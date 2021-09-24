## Home Work 1 CS441
#### Vivek Mishra
#### University of Illinois at Chicago

### Introduction

In this assignment our aim is to analyze and build various cloud architectures with multiple datacenters each of 
which offers different mixes of SaaS, PaaS, IaaS and FaaS model implementations with various pricing criteria,
 each of the datacenter having multiple hosts, host network topology, multiple VMs along with its allocation policies , 
Cloudlet scheduling policies and finally the costs associated with cloudlets and VMs on this cloud infrastructure. 

CloudSim Plus is a modern, up-to-date, full-featured and fully documented simulation framework. It’s easy to 
use and extend, enabling modeling, simulation, and experimentation of Cloud computing infrastructures and application
services. Based on the CloudSim framework, it aims to improve several engineering aspects, such as maintainability, 
reusability and extensibility.

### Installation instructions
This section contains the instructions on how to run the simulations implemented as part of this assignment,

1. Clone the repository to your local machine using

    `$ git clone git@github.com:vivek721/CloudOrg-Simulator.git`
3. Go to the cloned repository location on your terminal and test the program using sbt clean compile test
4. WGo to the cloned repository location on your terminal and compile the program using sbt clean compile run
5. The output prints a table of results for execution of 500 cloudlets.

Note: The cloudsim framework has been compiled in jar files and added in the lib/ folder of the project. 
Please note that, although IntellJ IDEA shall recognise the dependencies automatically, this may sometimes fail. 
When this happens go to “File -> Project Structure”, select “Libraries” on the right-hand side and add the provided jars manually.

### Project structure

In this section the project structure is described.

#### Simulations

The following Simulations are provided:

- IaaSDatacenterSim: Simulation showing IaaS functionality with several VM and DatacenterBroker policies.
- PaaSDatacenterSim: Simulation showing PaaS functionality
- SaaSDatacenterSim: Simulation showing SaaS functionality

#### Tests

The following test classes are provided:

- IaaSDatacenterSimTestSuite
- PaaSDatacenterSimTestSuite
- SaaSDatacenterSimTestSuite

### Configuration parameters
The use of hardcoded values is limited in the source code as they limit code reuse and readability.
The configuration file used for different simulations are  "simulationIaaS.conf", simulationPaaS.conf and simulationSaaS.conf.

![alt text](src/main/resources/IaaS-PaaS-SaaS.png)

####In the IaaS model Implementation:

Provider:- The provider provides the basic infrastructure like Servers, storage, networks 

Consumer:- The consumer selects the Operating system, runtime, cloudlets and the VM specification.

####In the PaaS model Implementation:
Provider:- The provider has control over the datacenter and its Servers, storage, networks along with this he also has control over 
middleware, OS and the runtime. 

Consumer:- The consumer has the control over the application and its data apart from this the consumer has no control.

####In the SaaS model Implementation:
Provider:- The provider has control on all datacenter and its resouce along with this they also have conrol over the application.

Consumer:- The consumer has no control over the application, they just provide the cloudlet specification such as cloudlet length.


### Evaluation and Analysis of the results

