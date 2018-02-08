/*
 * This file is part of KatLib, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2016 Katrix
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.github.katrix.katlib

import java.nio.file.Path

import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService}

import org.slf4j.Logger
import org.spongepowered.api.Sponge
import org.spongepowered.api.plugin.{PluginContainer => SpongePluginContainer}
import org.spongepowered.api.scheduler.SpongeExecutorService

import io.github.katrix.katlib.command.CmdPlugin
import io.github.katrix.katlib.helper.Implicits.PluginContainer

abstract class ImplKatPlugin(val logger: Logger, val configDir: Path, spongeContainer: SpongePluginContainer)
    extends KatPlugin {

  val container: PluginContainer = spongeContainer

  lazy val syncExecutor:         SpongeExecutorService           = Sponge.getScheduler.createSyncExecutor(this)
  lazy val syncExecutionContext: ExecutionContextExecutorService = ExecutionContext.fromExecutorService(syncExecutor)

  final val pluginCmd = new CmdPlugin()(this)
}
