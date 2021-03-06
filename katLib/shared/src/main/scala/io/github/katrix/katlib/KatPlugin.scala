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

import scala.concurrent.ExecutionContextExecutorService

import org.slf4j.Logger
import org.spongepowered.api.scheduler.SpongeExecutorService
import org.spongepowered.api.service.pagination.PaginationList
import org.spongepowered.api.text.channel.MessageReceiver

import io.github.katrix.katlib.command.CmdPlugin
import io.github.katrix.katlib.helper.Implicits.PluginContainer
import io.github.katrix.katlib.helper.LogHelper
import io.github.katrix.katlib.persistant.Config

trait KatPlugin { self =>

  def logger: Logger

  def configDir: Path

  def container: PluginContainer

  def syncExecutor: SpongeExecutorService

  def syncExecutionContext: ExecutionContextExecutorService

  /**
		* A root command providing stuff like the help and info command
		*/
  def pluginCmd: CmdPlugin

  /**
		* The config for the plugin
		*/
  def config: Config

  /**
    * A version adapter that concerns stuff in KatLib itself.
    */
  def globalVersionAdapter: GlobalVersionAdapter = new GlobalVersionAdapter {

    //Default possibly not working implementation
    override def sendPagination(paginationList: PaginationList.Builder, receiver: MessageReceiver): Unit = {
      LogHelper.warn("Used default global version helper. This can cause errors in newer API releases.")(self)
      paginationList.sendTo(receiver)
    }
  }
}
