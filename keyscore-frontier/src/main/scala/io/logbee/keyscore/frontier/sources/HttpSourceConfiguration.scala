package io.logbee.keyscore.frontier.sources

import java.util.UUID


case class HttpSourceConfiguration(
                                    bindAddress: Option[String] = None,
                                    port: Option[Int] = None,
                                    fieldName: Option[String] = None
                                  )
