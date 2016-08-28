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
package io.github.katrix.katlib.helper

import java.nio.file.Path
import java.util.Optional

import scala.collection.JavaConverters._
import scala.reflect.ClassTag

import org.slf4j.Logger
import org.spongepowered.api.asset.Asset
import org.spongepowered.api.plugin.{PluginContainer => SpongePluginContainer}
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.format.{TextColor, TextColors, TextStyle}

import com.google.common.reflect.TypeToken

import ninja.leaping.configurate.ConfigurationNode

object Implicits {

	implicit class RichString(val string: String) extends AnyVal {

		def text: Text = Text.of(string)
		def richText: RichText = Text.of(string)
	}


	implicit class RichText(val textOf: Text) extends AnyVal {

		def error(): Text = color(TextColors.RED).textOf
		def success(): Text = color(TextColors.GREEN).textOf
		def info(): Text = color(TextColors.YELLOW).textOf

		def color(textColor: TextColor): RichText = textOf.toBuilder.color(textColor).build()
		def style(textStyle: TextStyle): RichText = textOf.toBuilder.style(textStyle).build()
	}

	implicit class RichOptional[A](val optional: Optional[A]) extends AnyVal {

		def toOption: Option[A] = {
			if(optional.isPresent) {
				Some(optional.get())
			}
			else {
				None
			}
		}
	}

	implicit class Castable(val obj: AnyRef) extends AnyVal {

		def asInstanceOfOpt[T <: AnyRef : ClassTag] = {
			obj match {
				case t: T => Some(t)
				case _ => None
			}
		}
	}

	implicit class PluginContainer(val container: SpongePluginContainer) extends AnyVal {

		def id: String = container.getId
		def name: String = container.getName
		def version: Option[String] = container.getVersion.toOption
		def description: Option[String] = container.getDescription.toOption
		def url: Option[String] = container.getUrl.toOption
		def authors: Seq[String] = container.getAuthors.asScala
		def assetDirectory: Option[Path] = container.getAssetDirectory.toOption
		def getAsset(name: String): Option[Asset] = container.getAsset(name).toOption
		def sources: Option[Path] = container.getSource.toOption
		def instance: Option[_] = container.getInstance().toOption
		def logger: Logger = container.getLogger
	}

	implicit def typeToken[A: ClassTag]: TypeToken[A] = TypeToken.of(implicitly[ClassTag[A]].runtimeClass.asInstanceOf[Class[A]])

	/*
	implicit def typeTokenMacro[A]: TypeToken[A] = macro typeTokenMacroImpl

	import scala.reflect.macros.whitebox

	def typeTokenMacroImpl[A](c: whitebox.Context)(implicit wtt: c.WeakTypeTag[A]): c.Expr[TypeToken[A]] = {
		import c.universe._
		c.Expr[TypeToken[A]](q"TypeToken.of(classOf[$wtt])")
	}
	*/

	implicit class RichConfigurationNode(val node: ConfigurationNode) extends AnyVal {

		//Pretty syntax is bugged right now https://issues.scala-lang.org/browse/SI-8969
		def value_=[A: TypeToken](value: A): Unit = node.setValue(implicitly[TypeToken[A]], value)
		def value[A: TypeToken]: A = node.getValue(implicitly[TypeToken[A]])

		def list[A: TypeToken]: Seq[A] = Seq(node.getList(implicitly[TypeToken[A]]).asScala: _*)
	}
}