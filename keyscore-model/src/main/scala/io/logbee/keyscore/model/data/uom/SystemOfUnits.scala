package io.logbee.keyscore.model.data.uom

import io.logbee.keyscore.model.data.uom.SystemOfUnits.Quantities.DigitalInformationStorage
import io.logbee.keyscore.model.data.uom.SystemOfUnits.SI.Prefixes._
import io.logbee.keyscore.model.data.uom.SystemOfUnits.IEEE.Prefixes._

import io.logbee.keyscore.model.util.ToOption.T2OptionT

object SystemOfUnits {

  object Quantities {
    val Length = Quantity("LENGTH")
    val Time = Quantity("TIME")
    val Mass = Quantity("MASS")
    val ElectricCurrent = Quantity("ELECTRIC_CURRENT")
    val ThermodynamicTemperature = Quantity("THERMODYNAMIC_TEMPERATURE")
    val AmountOfSubstance = Quantity("AMOUNT_OF_SUBSTANCE")
    val LuminousIntensity = Quantity("LUMINOUS_INTENSITY")
    val DigitalInformationStorage = Quantity("DIGITAL_INFORMATION_STORAGE")
  }

  object SI {

    object Prefixes {
      val Peta  = Prefix("P", 10, 15)
      val Tera  = Prefix("T", 10, 12)
      val Giga  = Prefix("G", 10, 9)
      val Mega  = Prefix("M", 10, 6)
      val Kilo  = Prefix("k", 10, 3)
      val Deci  = Prefix("d", 10, -1)
      val Centi = Prefix("c", 10, -2)
      val Milli = Prefix("m", 10, -3)
      val Micro = Prefix("µ", 10, -12)
      val Nano  = Prefix("n", 10, -9)
      val Pico  = Prefix("p", 10, -12)
    }

    val Meter: Unit = Unit("m", Quantities.Length)
    val KiloGram: Unit = Unit("kg", Quantities.Mass)
    val Second: Unit = Unit("s", Quantities.Time)
    val Ampere: Unit = Unit("A", Quantities.ElectricCurrent)
    val Kelvin: Unit = Unit("K", Quantities.ThermodynamicTemperature)
    val Mol: Unit = Unit("mol", Quantities.AmountOfSubstance)
    val Candela: Unit = Unit("cd", Quantities.LuminousIntensity)
  }

  object IEEE {

    object Prefixes {
      val Pebi  = Prefix("Pi", 2, 50)
      val Tebi  = Prefix("Ti", 2, 40)
      val Gibi  = Prefix("Gi", 2, 30)
      val Mebi  = Prefix("Mi", 2, 20)
      val Kibi  = Prefix("Ki", 2, 10)
    }

    val Bit: Unit = Unit("bit", DigitalInformationStorage)
    val Byte: Unit = Unit("B", DigitalInformationStorage)
  }

  object Predef {

    // Length - Meter
    val KiloMeter: Unit = Unit("km", Kilo, Derived(SI.Meter))
    val DeciMeter: Unit = Unit("dm", Deci, Derived(SI.Meter))
    val CentiMeter: Unit = Unit("cm", Centi, Derived(SI.Meter))
    val MilliMeter: Unit = Unit("mm", Milli, Derived(SI.Meter))
    val MicroMeter: Unit = Unit("µm", Micro, Derived(SI.Meter))
    val NanoMeter: Unit = Unit("nm", Nano, Derived(SI.Meter))

    // Time - Second
    val MilliSecond: Unit = Unit("ms", Milli, Derived(SI.Second))
    val MicroSecond: Unit = Unit("µs", Micro, Derived(SI.Second))
    val NanoSecond: Unit = Unit("ns", Nano, Derived(SI.Second))

    // Time - Derived from Second
    val Day: Unit = Unit("d", Derived(SI.Second, 1, 86400, 0))
    val Hour: Unit = Unit("h", Derived(SI.Second, 1, 3600, 0))
    val Minute: Unit = Unit("min", Derived(SI.Second, 1, 60, 0))

    // DigitalInformationStorage - Bit
    val PetaBit: Unit = Unit("Pbit", Peta, Derived(IEEE.Bit))
    val TeraBit: Unit = Unit("Tbit", Tera, Derived(IEEE.Bit))
    val GigaBit: Unit = Unit("Gbit", Giga, Derived(IEEE.Bit))
    val MegaBit: Unit = Unit("Mbit", Mega, Derived(IEEE.Bit))
    val KiloBit: Unit = Unit("kbit", Kilo, Derived(IEEE.Bit))

    val PebiBit: Unit = Unit("Pibit", Pebi, Derived(IEEE.Bit))
    val TebiBit: Unit = Unit("Tibit", Tebi, Derived(IEEE.Bit))
    val GibiBit: Unit = Unit("Gibit", Gibi, Derived(IEEE.Bit))
    val MebiBit: Unit = Unit("Mibit", Mebi, Derived(IEEE.Bit))
    val KibiBit: Unit = Unit("Kibit", Kibi, Derived(IEEE.Bit))

    // DigitalInformationStorage - Byte
    val PetaByte: Unit = Unit("PB", Peta, Derived(IEEE.Byte))
    val TeraByte: Unit = Unit("TB", Tera, Derived(IEEE.Byte))
    val GigaByte: Unit = Unit("GB", Giga, Derived(IEEE.Byte))
    val MegaByte: Unit = Unit("MB", Mega, Derived(IEEE.Byte))
    val KiloByte: Unit = Unit("kB", Kilo, Derived(IEEE.Byte))

    val PebiByte: Unit = Unit("PiB", Tebi, Derived(IEEE.Byte))
    val TebiByte: Unit = Unit("TiB", Tebi, Derived(IEEE.Byte))
    val GibiByte: Unit = Unit("GiB", Gibi, Derived(IEEE.Byte))
    val MebiByte: Unit = Unit("MiB", Mebi, Derived(IEEE.Byte))
    val KibiByte: Unit = Unit("KiB", Kibi, Derived(IEEE.Byte))
  }
}
