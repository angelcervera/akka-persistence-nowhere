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

import akka.actor.{ActorSystem, Kill, Props}
import akka.pattern.ask
import akka.persistence.{PersistentActor, SnapshotOffer}
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import com.acervera.akka.persistence.nowhere.NowhereActorClassicTest._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.Await
import scala.concurrent.duration._




object NowhereActorClassicTest {
  case class State(events: Long, acc: Long)

  def props: Props = Props(new NowhereActorClassicTest)

  case class Cmd(v: Long)

  case class Evt(v: Long)

  case object GetState

}

class NowhereActorClassicTest extends PersistentActor {

  var state = State(0, 0)

  override def persistenceId: String = "test"

  override def receiveRecover: Receive = {
    case evt: Evt => updateState(evt)
    case SnapshotOffer(_, snapshot: State) => state = snapshot
  }

  val snapShotInterval = 5

  override def receiveCommand: Receive = {
    case Cmd(v) =>
      persist(Evt(v)) { event =>
        updateState(event)
        context.system.eventStream.publish(event)
        if (lastSequenceNr % snapShotInterval == 0 && lastSequenceNr != 0) {
          saveSnapshot(state)
        }
      }
    case GetState => sender ! state
  }

  private def updateState(event: Evt): Unit = {
    state = State(state.events + 1, state.acc + event.v)
  }

}

class NowhereActorSpec
  extends TestKit(ActorSystem("NowhereActorSpec"))
    with ImplicitSender
    with Matchers
    with WordSpecLike
    with BeforeAndAfterAll {

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "Using Persistence Classic" should {
    "not persist anything" in {

      val nowhereActor = system.actorOf(NowhereActorClassicTest.props)
      1 to 100 foreach (_ => nowhereActor ! Cmd(10))

      implicit val timeout = Timeout(3 seconds)
      val state = Await
        .result(nowhereActor ? GetState, timeout.duration)
        .asInstanceOf[State]

      state should be(State(100, 1000))

      nowhereActor ! Kill
      Thread.sleep(1000)

      val nowhereActor2 = system.actorOf(NowhereActorClassicTest.props)

      val state2 = Await
        .result(nowhereActor2 ? GetState, timeout.duration)
        .asInstanceOf[State]

      state2 should be(State(0, 0))
    }
  }
}

