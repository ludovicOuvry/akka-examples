package net.iryndin.akka.examples.actordemos;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;

/**
 * Demonstrates creation of actor with actorOf method
 */
public class ActorCreationDemo2 {

    public static void main( String[] args ) throws Exception {
        final ActorSystem actorSystem = ActorSystem.create("actorCreationDemo1AS");
        final ActorRef actor = actorSystem.actorOf(new Props(Actor1.class));
        actor.tell("Hello actor!", null);
        Thread.sleep(1000);
        actorSystem.shutdown();
    }

    static class Actor1 extends UntypedActor {

        final String actorName = "actor1";

        @Override
        public void onReceive(Object message) throws Exception {
            System.out.println(actorName + " got: " + message);
        }

        @Override
        public void preStart() {
            super.preStart();
            System.out.println(actorName + " started");
        }

        @Override
        public void postStop() {
            super.postStop();
            System.out.println(actorName + " stopped");
        }
    }
}
