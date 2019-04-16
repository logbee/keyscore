package io.logbee.keyscore.commons.cluster

/**
  * Holds all values of the Cluster Topics
  */
object Topics {
  /** Topic for all WhoIs requests and their HereIAm responses. */
  val WhoIsTopic = "WHOIS_TOPIC"
  /** Topic for all cluster wide messages from members with the role "agent". */
  val AgentsTopic = "AGENTS_TOPIC"
  /** Topic for all cluster wide messages for managing the cluster members. */
  val ClusterTopic = "CLUSTER_TOPIC"
  /** Topic for Filter and Pipeline Metrics */
  val MetricsTopic = "METRICS_TOPIC"
  /** Topic for all Metrics Request */
  val FilterMetricsTopic = "FILTER_METRICS_TOPIC"
}