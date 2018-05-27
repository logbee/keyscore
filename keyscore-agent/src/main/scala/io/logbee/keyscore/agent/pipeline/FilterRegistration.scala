package io.logbee.keyscore.agent.pipeline

import io.logbee.keyscore.model.filter.MetaFilterDescriptor

case class FilterRegistration(filterDescriptor: MetaFilterDescriptor, filterClass: Class[_])
