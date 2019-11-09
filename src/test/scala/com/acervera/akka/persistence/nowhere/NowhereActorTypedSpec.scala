/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Ángel Cervera Claudio
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

// scalastyle:off magic.number

package com.acervera.akka.persistence.nowhere

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.typed.{ActorSystem, Behavior, SupervisorStrategy}
import akka.pattern.ask
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}
import akka.persistence.{PersistentActor, SnapshotOffer}
import akka.stream.ActorAttributes.SupervisionStrategy
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import akka.actor.typed.{ActorRef, Behavior, SupervisorStrategy}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}
import com.acervera.akka.persistence.nowhere.NowhereActorTypedTesting.{GetState, Increment, State}

import scala.concurrent.Await
import scala.concurrent.duration._

object NowhereActorTypedTesting {

  case class State(events: Long, acc: Long)

  trait Command
  case class Increment(v: Long) extends Command
  case class GetState(replyTo: ActorRef[State]) extends Command

  trait Event
  case class Incremented(v: Long) extends Event

  def apply(): Behavior[Command] =
    EventSourcedBehavior[Command, Event, State](
      persistenceId = PersistenceId.ofUniqueId("testingTypedId"),
      emptyState = State(0,0),
      commandHandler = (state, command) => onCommand(state, command),
      eventHandler = (state, event) => applyEvent(state, event)
    )
    .snapshotWhen {
      case (state: State, event: Event, 5) => true
      case (state: State, event: Event, seqNumber) => false
    }

  private def onCommand(state: State, cmd: Command): Effect[Event, State] = cmd match {
    case GetState(replyTo) =>
      replyTo ! state
      Effect.none

    case Increment(v) =>
      Effect.persist(Incremented(v))
  }

  private def applyEvent(state: State, event: Event): State = event match {
    case Incremented(v) =>
      State(state.events +1, state.acc + v)
  }

}

class NowhereActorTypedSpec
  extends ScalaTestWithActorTestKit
    with WordSpecLike
    with Matchers {

  "Using Persistence Typed" should {
      "not persist anything" in {
        implicit val timeout = Timeout(3 seconds)

        val nowhereActor = testKit.spawn(NowhereActorTypedTesting(), "testing")
        1 to 100 foreach (_ => nowhereActor ! Increment(10))

        val probe = testKit.createTestProbe[State]()
        nowhereActor ! GetState(probe.ref)

        probe.expectMessage(State(100, 1000))

        testKit.stop(nowhereActor)
        Thread.sleep(1000)

        val nowhereActor2 = testKit.spawn(NowhereActorTypedTesting(), "testing")
        nowhereActor2 ! GetState(probe.ref)

        probe.expectMessage(State(0, 0))
      }
  }

}

