package io.github.katrix.katlib.data

import scala.collection.immutable
import scala.collection.mutable

trait ImmutableSeqValue[A] extends ImmutableIterableValue[A, immutable.Seq[A]] {

	override type Self = ImmutableSeqValue[A]
	override type MutableCollection <: mutable.Seq[A]
	override type Mutable = SeqValue[A]

	def apply(index: Int): A
	def head: A
	def tail: immutable.Seq[A]
}

trait SeqValue[A] extends IterableValue[A, mutable.Seq[A]] {

	override type Self = SeqValue[A]
	override type ImmutableCollection <: immutable.Seq[A]
	override type Immutable = ImmutableSeqValue[A]

	def apply(index: Int): A
	def head: A
	def tail: mutable.Seq[A]
}