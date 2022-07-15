/*
 * Copyright 2013 Toshiyuki Takahashi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flywaydb.play

import javax.inject._
import play.api._
import play.core._

@Singleton
class PlayInitializer @Inject() (
    configuration: Configuration,
    environment: Environment,
    flyways: Flyways,
    webCommands: WebCommands
) {

  def onStart(): Unit = {
    val webCommand = new FlywayWebCommand(configuration, environment, flyways)
    webCommands.addHandler(webCommand)

    flyways.allDatabaseNames.foreach { dbName =>
      environment.mode match {
        case Mode.Test =>
          flyways.migrate(dbName)
        case Mode.Prod if flyways.config(dbName).auto =>
          flyways.migrate(dbName)
        case Mode.Prod =>
          flyways.checkState(dbName)
        case Mode.Dev =>
        // Do nothing here.
        // In dev mode, FlywayWebCommand handles migration.
      }
    }
  }

  val enabled: Boolean =
    !configuration.getOptional[String]("flywayplugin").contains("disabled")

  if (enabled) {
    onStart()
  }

}
