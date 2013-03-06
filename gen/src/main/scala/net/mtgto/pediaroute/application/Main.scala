package net.mtgto.pediaroute.application

import net.mtgto.pediaroute.domain.DataGenerateService

object DataGenerator extends App {
  val service = new DataGenerateService
  service.generate
}