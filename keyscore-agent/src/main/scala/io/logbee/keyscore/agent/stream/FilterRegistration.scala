package io.logbee.keyscore.agent.stream

import io.logbee.keyscore.model.filter.MetaFilterDescriptor

case class FilterRegistration(filterDescriptor: MetaFilterDescriptor, filterClass: Class[_])
