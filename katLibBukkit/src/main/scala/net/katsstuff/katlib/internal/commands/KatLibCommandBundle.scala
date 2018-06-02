package net.katsstuff.katlib.internal.commands

import java.util.UUID

import scala.collection.mutable
import scala.concurrent.duration._

import org.bukkit.command.CommandSender

import cats.data.NonEmptyList
import cats.effect.Sync
import cats.kernel.Monoid
import cats.syntax.all._
import cats.{~>, MonadError}
import net.katsstuff.katlib.commands.BukkitKatLibCommands
import net.katsstuff.katlib.internal.util.Zipper
import net.katsstuff.minejson.text._
import net.katsstuff.scammander.CommandFailure
import net.katsstuff.scammander.bukkit.components.BukkitExtra
import net.katstuff.katlib.algebras.{Cache, CommandSourceAccess, Localized, Pagination}

abstract class KatLibCommandBundle[F[_]: Sync, G[_], Page: Monoid](FtoG: F ~> G)(
    implicit
    pagination: Pagination.Aux[F, CommandSender, Page],
    localized: Localized[F, CommandSender],
    C: Cache[F],
    CS: CommandSourceAccess[F, CommandSender],
    override val F: MonadError[G, NonEmptyList[CommandFailure]],
) extends BukkitKatLibCommands[F, G, Page](pagination, FtoG, localized) {

  object PageCmd extends Command[CommandSender, PageArgs] {

    private val allPages = new mutable.WeakHashMap[CommandSender, C.CacheType[UUID, Zipper[Text]]].withDefault { _ =>
      C.createExpireAfterWrite[UUID, Zipper[Text]](5.minutes)
    }

    private val focusedPages = new mutable.WeakHashMap[CommandSender, UUID]

    override def run(sender: CommandSender, extra: BukkitExtra, arg: PageArgs): G[CommandSuccess] = {

      val uuidG = arg.uuid.toF("No active page")

      val pageData = arg.specifier match {
        case Some(PageArgsSpecifier.Prev) =>
          for {
            uuid    <- uuidG
            optPage <- FtoG(prevPage(sender, uuid))
            page    <- optPage.toF("No previous page")
          } yield page
        case Some(PageArgsSpecifier.Next) =>
          for {
            uuid    <- uuidG
            optPage <- FtoG(nextPage(sender, uuid))
            page    <- optPage.toF("No next page")
          } yield page
        case Some(PageArgsSpecifier.Page(num)) =>
          for {
            uuid    <- uuidG
            optPage <- FtoG(gotoPage(sender, uuid, num))
            page    <- optPage.toF(s"No page at $num")
          } yield page
        case None =>
          for {
            uuid    <- uuidG
            optPage <- FtoG(currentPage(sender, uuid))
            page    <- optPage.toF(s"No page found for $uuid")
          } yield page
      }

      pageData.flatMap(page => FtoG(CS.sendMessage(sender, page))).as(Command.success())
    }

    def currentPage(sender: CommandSender, uuid: UUID): F[Option[Text]] =
      C.get(allPages(sender))(uuid).map(opt => opt.map(_.focus))

    private def getPage(
        sender: CommandSender,
        uuid: UUID,
        action: Zipper[Text] => Option[Zipper[Text]]
    ): F[Option[Text]] = {
      import cats.instances.option._

      C.get(allPages(sender))(uuid).flatMap { optZipper =>
        val res = for {
          zipper <- optZipper
          next   <- action(zipper)
        } yield {
          val setAllPages     = C.put(allPages(sender))(uuid, next)
          val setFocusedPages = Sync[F].delay(focusedPages.put(sender, uuid))

          setAllPages *> setFocusedPages.as(next.focus)
        }

        res.sequence
      }
    }

    def nextPage(sender: CommandSender, uuid: UUID):         F[Option[Text]] = getPage(sender, uuid, _.right)
    def prevPage(sender: CommandSender, uuid: UUID):         F[Option[Text]] = getPage(sender, uuid, _.left)
    def gotoPage(sender: CommandSender, uuid: UUID, i: Int): F[Option[Text]] = getPage(sender, uuid, _.goto(i))

    def newPages(sender: CommandSender, pages: UUID => Seq[Text]): F[Text] = {
      val uuid         = UUID.randomUUID()
      val createdPages = pages(uuid)
      val zipper       = Zipper(Nil, createdPages.head, createdPages.tail)

      val senderPages = allPages(sender)

      val setSenderPages  = C.put(senderPages)(uuid, zipper)
      val setFocusedPages = Sync[F].delay(focusedPages.put(sender, uuid))
      val setAllPages     = Sync[F].delay(allPages.put(sender, senderPages))

      setSenderPages *> setFocusedPages *> setAllPages.as(createdPages.head)
    }
  }

  case class PageArgs(specifier: Option[PageArgsSpecifier], uuid: Option[UUID])
  object PageArgs {
    implicit val param: Parameter[PageArgs] = ParameterDeriver[PageArgs].derive
  }

  sealed trait PageArgsSpecifier
  object PageArgsSpecifier {
    case object Next           extends PageArgsSpecifier
    case object Prev           extends PageArgsSpecifier
    case class Page(page: Int) extends PageArgsSpecifier

    implicit val nextParam: Parameter[Next.type] = Parameter.mkSingleton(Next)
    implicit val prevParam: Parameter[Prev.type] = Parameter.mkSingleton(Prev)
    implicit val pageParam: Parameter[Page]      = ParameterDeriver[Page].derive

    implicit val specifierParam: Parameter[PageArgsSpecifier] = ParameterDeriver[PageArgsSpecifier].derive
  }

  object CallbackCmd extends Command[CommandSender, String] {

    private val callbacks = C.createExpireAfterWrite[String, CommandSender => F[Unit]](5.minutes)

    override def run(source: CommandSender, extra: BukkitExtra, arg: String): G[CommandSuccess] =
      for {
        optCallback <- FtoG(C.get(callbacks)(arg))
        callback    <- optCallback.toF("No callback found")
        _           <- FtoG(callback(source))
      } yield Command.success()

    def createCallback(callback: CommandSender => F[Unit]): F[String] = {
      val uuid = UUID.randomUUID().toString
      C.put(callbacks)(uuid, callback).as(uuid)
    }
  }
}
