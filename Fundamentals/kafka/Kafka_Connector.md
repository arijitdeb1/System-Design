## Kafka Connect

* Kafka Connect is a framework for connecting Kafka with external systems such as databases, key-value stores, search indexes, and file systems. It allows you to pull data from these systems into Kafka, or push data from Kafka into these systems.
* Source Connectors - Source connectors are used to pull data from an external system into Kafka.
* Sink Connectors - Sink connectors are used to push data from Kafka into an external system.
* Kafka Connect is a part of Apache Kafka and is available as a part of the Kafka distribution.
* Kafka Connect Cluster - A Kafka Connect cluster is a group of Kafka Connect workers that work together to execute connectors and tasks.
* `Connectors`::
  * Kafka Connect Cluster has multiple loaded Connectors. 
  * Each Connector is a reusable piece of code(jar file). 
  * Many connectors are available in the Confluent Hub.
* `Tasks` ::
   * Each Connector has one or more Tasks.
   * Connector tasks are responsible for moving data between Kafka and the external system.
   * Connectors + User Configuration = Tasks
* `Workers` ::
   * Kafka Connect Cluster has multiple Workers.
   * Each Worker is a separate JVM process.
   * Each Worker can run multiple Connectors.
   * Each Worker can run multiple Tasks.
* `Standalone Mode` - 
  * In standalone mode, Kafka Connect runs as a single process/worker.
  * It is useful for development and testing.
  * Not fault-tolerant, no scalability, hard to monitor.
* `Distributed Mode` - 
  * In distributed mode, Kafka Connect runs as a cluster of workers.
  * It is useful for production.
  * Fault-tolerant, scalable, easy to monitor.
  * Configuration is submitted using REST API.
  * Rebalancing of tasks between workers is done automatically when a worker is added or removed.
