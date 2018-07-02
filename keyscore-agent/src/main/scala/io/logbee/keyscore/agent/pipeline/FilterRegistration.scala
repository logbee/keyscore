package io.logbee.keyscore.agent.pipeline

import com.typesafe.config.Config
import io.logbee.keyscore.model.filter.MetaFilterDescriptor

case class FilterRegistration(filterDescriptor: MetaFilterDescriptor, filterClass: Class[_], filterConfiguration: Option[Config])
