package net.iryndin.akka.examples.actordemos;

import akka.actor.*;

/**
 * Demonstrates creation of actor with UntypedActorFactory
 * Will fetch Google PageRank for a domain
 */
public class ActorWatchdogDemo {

    public static void main( String[] args ) throws Exception {
        final ActorSystem actorSystem = ActorSystem.create("as1");
        final ActorRef actor = actorSystem.actorOf(new Props(WatchDogActor.class));
        actor.tell("start", null);
        Thread.sleep(5000);
        actor.tell("killall", null);
        Thread.sleep(5000);
        actorSystem.shutdown();
    }

    /**
     * WatchDog Actor which watches all child actors and gets Terminated message from them
     */
    static class WatchDogActor extends UntypedActor {

        final int N = 10;
        final String actorName = "WatchDogActor";

        final ActorRef[] watchedActors = new ActorRef[N];

        public WatchDogActor() {
            createWatchedActors();
        }

        private void createWatchedActors() {
            for (int i=0; i<N; i++) {
                final int j = i;
                watchedActors[i] = getContext().actorOf(new Props(new UntypedActorFactory() {
                    @Override
                    public Actor create() throws Exception {
                        return new WorkerActor(j);
                    }
                }), "worker"+j);
                getContext().watch(watchedActors[i]);
            }
        }

        @Override
        public void onReceive(Object message) throws Exception {
            if (message.equals("start")) {
                sendAllChildren("start");
            } else if (message.equals("killall")) {
                for (int i=0; i<N; i++) {
                    getContext().stop(watchedActors[i]);
                }
            } else if (message instanceof Terminated) {
                final Terminated t = (Terminated) message;
                for (int i=0; i<N; i++) {
                    if (watchedActors[i] == t.actor()) {
                        System.out.println("worker"+ i + " completed");
                    }
                }
            } else {
                System.out.println("Watchdog get: " + message);
                unhandled(message);
            }
        }

        private void sendAllChildren(String s) {
            for (ActorRef a : watchedActors) {
                a.tell(s, getSelf());
            }
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

    static class WorkerActor extends UntypedActor {

        private final int number;

        public WorkerActor(final int number) {
            this.number = number;
        }

        @Override
        public void onReceive(Object message) throws Exception {
            if (message.equals("start")) {
                doNumberCrunching();
            } else {
                unhandled(message);
            }
        }

        private void doNumberCrunching() throws InterruptedException {
            for (int i=0; i<Integer.MAX_VALUE; i++) {
                double k = i/23213.123213;
            }
        }

        @Override
        public void preStart() {
            System.out.println("Start Worker " + number);
        }

        @Override
        public void postStop() {
            System.out.println("Stop Worker " + number);
        }
    }
}
