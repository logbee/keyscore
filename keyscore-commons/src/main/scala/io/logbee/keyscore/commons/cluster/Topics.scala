package io.logbee.keyscore.commons.cluster

/**
  * Holds all values of the Cluster Topics
  */
object Topics {
  /** Topic for all WhoIs requests and their HereIAm responses. */
  val WhoIsTopic = "whois"
  /** Topic for all cluster wide messages from members with the role "agent". */
  val AgentsTopic = "agents"
  /** Topic for all cluster wide messages for managing the cluster members. */
  val ClusterTopic = "cluster"
}

/**
  * Holds all values for the remote Actor Selection
  */
object Paths {
  /** References to a Instance of the LocalPipelineManager. */
  val LocalPipelineManagerPath = "LocalPipelineManager"

  /** References to a Instance of the PipelineScheduler .*/
  val PipelineSchedulerPath = "PipelineScheduler"
}
