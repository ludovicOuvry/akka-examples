package net.iryndin.akka.examples.actordemos;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;

/**
 * Demonstrates creation of actor with actorFor
 */
public class ActorCreationDemo2 {

    static final String actorSystemName = "actorCreationDemo2AS";
    static final String actorName = "actor2";

    public static void main( String[] args ) throws Exception {
        final ActorSystem actorSystem = ActorSystem.create(actorSystemName);
        actorSystem.actorOf(new Props(Actor2.class), actorName);
        final String path = "akka://"+actorSystemName+"/user/" + actorName;
        System.out.println("Search actor by path: " + path);
        final ActorRef actor = actorSystem.actorFor(path);
        actor.tell("Hello actor!", null);
        Thread.sleep(1000);
        actorSystem.shutdown();
    }

    static class Actor2 extends UntypedActor {

        @Override
        public void onReceive(Object message) throws Exception {
            System.out.println(actorName + " got: " + message);
        }

        @Override
        public void preStart() {
            System.out.println(actorName + " started");
        }

        @Override
        public void postStop() {
            System.out.println(actorName + " stopped");
        }
    }
}
