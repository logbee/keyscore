package akka.cluster

import akka.actor.Address

class MockableMember(
  protocol: String = "akka.tcp",
  system: String = "system",
  uid: Long = 1,
  upNumber: Int = 1,
  status: MemberStatus = MemberStatus.Up,
  roles: Set[String] = Set.empty
) extends Member(UniqueAddress(Address(protocol, system), uid), upNumber, status, roles) {}
