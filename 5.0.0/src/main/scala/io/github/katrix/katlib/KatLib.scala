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

import org.slf4j.Logger
import org.spongepowered.api.Sponge
import org.spongepowered.api.config.ConfigDir
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.game.state.{GameConstructionEvent, GameInitializationEvent}
import org.spongepowered.api.plugin.{Dependency, Plugin, PluginContainer}

import com.google.inject.Inject

import io.github.katrix.katlib.helper.Implicits.RichOptional
import io.github.katrix.katlib.lib.LibKatLibPlugin
import io.github.katrix.katlib.persistant.KatLibTypeSerializers

object KatLib {

  final val CompiledAgainst = "5.0.0"
  final val Version         = s"2.3.0-$CompiledAgainst"
  final val ConstantVersion = "2.3.0-5.0.0"
  assert(Version == ConstantVersion)

  private var _plugin: KatLib = _
  implicit def plugin: KatLib = _plugin
}

@Plugin(
  id = LibKatLibPlugin.Id,
  name = LibKatLibPlugin.Name,
  version = KatLib.ConstantVersion,
  authors = Array("Katrix"),
  dependencies = Array(new Dependency(id = "spongeapi", version = KatLib.CompiledAgainst))
)
class KatLib @Inject()(logger: Logger, @ConfigDir(sharedRoot = true) configDir: Path, container: PluginContainer)
    extends ImplKatPlugin(logger, configDir, container) with KatLibBase {

  @Listener
  def gameConstruct(event: GameConstructionEvent): Unit = {
    KatLib._plugin = this
    KatLibTypeSerializers.registerScalaSerializers()
  }

  @Listener
  def gameInit(event: GameInitializationEvent): Unit = {
    checkSpongeVersion(Sponge.getPlatform.getApi.getVersion.toOption, KatLib.CompiledAgainst)
    pluginCmd.registerHelp()
    Sponge.getCommandManager.register(this, pluginCmd.commandSpec, pluginCmd.aliases: _*)
  }
}
