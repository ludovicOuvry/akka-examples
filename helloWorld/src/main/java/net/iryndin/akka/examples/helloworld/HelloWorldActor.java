package net.iryndin.akka.examples.helloworld;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;

public class HelloWorldActor extends UntypedActor
{
    public static void main( String[] args ) throws Exception {
        final ActorSystem actorSystem = ActorSystem.create("helloWorldAS");
        final ActorRef actor = actorSystem.actorOf(new Props(HelloWorldActor.class));
        actor.tell("Hello actor!", null);
        Thread.sleep(1000);
        actorSystem.shutdown();
    }

    @Override
    public void onReceive(Object message) throws Exception {
        System.out.println("Received: " + message);
        getSender().tell("I have got message: " + message, getSelf());
    }

    @Override
    public void preStart() {
        super.preStart();
        System.out.println("HelloWorldActor started");
    }

    @Override
    public void postStop() {
        super.postStop();
        System.out.println("HelloWorldActor stopped");
    }
}