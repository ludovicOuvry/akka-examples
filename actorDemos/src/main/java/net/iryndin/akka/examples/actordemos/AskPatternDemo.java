package net.iryndin.akka.examples.actordemos;

import akka.actor.*;
import akka.dispatch.Futures;
import akka.dispatch.Mapper;
import scala.concurrent.Future;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import static akka.pattern.Patterns.pipe;
import static akka.pattern.Patterns.ask;

/**
 * Demonstrates usage os ask pattern
 * Will fetch Google PageRank for a domain
 */
public class AskPatternDemo {

    static class MultiDomainRequest {
        public String[] domains;
    }

    static class DomainPagerankMessage {
        public final String domain;
        public final int pagerank;

        DomainPagerankMessage(String domain, int pagerank) {
            this.domain = domain;
            this.pagerank = pagerank;
        }
    }

    public static void main( String[] args ) throws Exception {
        final ActorSystem actorSystem = ActorSystem.create("as1");
        final ActorRef actor = actorSystem.actorOf(new Props(MasterActor.class));
        MultiDomainRequest req = new MultiDomainRequest();
        req.domains = new String[]{
           "facebook.com","vk.com","badoo.com","yahoo.com","techcrunch.com","akka.io","github.com"
        };
        actor.tell(req, null);
        Thread.sleep(5000);
        actorSystem.shutdown();
    }

    /**
     * WatchDog Actor which watches all child actors and gets Terminated message from them
     */
    static class MasterActor extends UntypedActor {

        final int TIMEOUT = 2000;
        final String actorName = "MasterActor";

        final ActorRef pagerankActor1 = getContext().actorOf(new Props(GooglePageRankActor.class));
        final ActorRef pagerankActor2 = getContext().actorOf(new Props(GooglePageRankActor.class));
        final ActorRef printerActor = getContext().actorOf(new Props(PrinterActor.class));


        @Override
        public void onReceive(Object message) throws Exception {
            if (message instanceof MultiDomainRequest) {
                final ArrayList<Future<Object>> futures = new ArrayList<>();

                final MultiDomainRequest req = (MultiDomainRequest)message;
                final int k = req.domains.length/2;
                for (int i=0; i<k;i++) {
                    futures.add(ask(pagerankActor1, req.domains[i], TIMEOUT));
                }
                for (int i=k; i<req.domains.length;i++) {
                    futures.add(ask(pagerankActor2, req.domains[i], TIMEOUT));
                }
                final Future<Iterable<Object>> aggregate = Futures.sequence(futures, getContext().dispatcher());

                final Future<String> transformed = aggregate.map(
                        new Mapper<Iterable<Object>, String>() {
                            public String apply(Iterable<Object> iterable) {
                                StringBuilder sb = new StringBuilder();
                                for (Object o : iterable) {
                                    final DomainPagerankMessage dpr = (DomainPagerankMessage)o;
                                    sb.append(dpr.domain).append(": ").append(dpr.pagerank).append("\n");
                                }
                                return sb.toString();
                            }
                        }, getContext().dispatcher());

                pipe(transformed, getContext().dispatcher()).to(printerActor);
            } else {
                unhandled(message);
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

    static class PrinterActor extends UntypedActor {

        @Override
        public void onReceive(Object o) throws Exception {
            System.out.println(o);
        }
    }

    /**
     * This actor gets Google Page Rank for a domain
     */
    static class GooglePageRankActor extends UntypedActor {

        final JenkinsHash jenkinsHash = new JenkinsHash();

        @Override
        public void onReceive(Object message) throws Exception {
            if (message instanceof String) {
                final String domain = (String)message;
                int pagerank = getPR(domain);
                getSender().tell(new DomainPagerankMessage(domain, pagerank),getSelf());
            } else {
                unhandled(message);
            }
        }

        public int getPR(final String domain) {
            final String url = buildUrl(domain);

            int result = 0;

            try {
                final URLConnection conn = new URL(url).openConnection();
                try (final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    String input;
                    while ((input = reader.readLine()) != null) {
                        // What Google returned? Example : Rank_1:1:9, PR = 9
                        String s = input.substring(input.lastIndexOf(":") + 1);
                        if (s.length()==0) {
                            result = 0;
                        } else {
                            result = Integer.parseInt(s);
                        }
                    }
                } catch (Exception e) {
                    result = 0;
                }
            } catch (IOException ioe) {
                result = 0;
            }
            return result;
        }

        private String buildUrl(final String domain) {
            final long hash = jenkinsHash.hash(("info:" + domain).getBytes());

            //Append a 6 in front of the hashing value.
            final String url = "http://toolbarqueries.google.com/tbr?client=navclient-auto&hl=en&"
                    + "ch=6" + hash + "&ie=UTF-8&oe=UTF-8&features=Rank&q=info:" + domain;
            return url;
        }

        @Override
        public void preStart() {
            System.out.println("GooglePageRankActor started");
        }

        @Override
        public void postStop() {
            System.out.println("GooglePageRankActor stopped");
        }
    }
}
