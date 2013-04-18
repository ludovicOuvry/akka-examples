package net.iryndin.akka.examples.actordemos;

import akka.actor.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * Demonstrates creation of actor with UntypedActorFactory
 * Will fetch Google PageRank for a domain
 */
public class ActorCreationDemo3 {

    static final String actorSystemName = "actorCreationDemo3AS";
    static final String actorName = "googlePageRankActor";

    public static void main( String[] args ) throws Exception {
        final ActorSystem actorSystem = ActorSystem.create(actorSystemName);
        final ActorRef actor = actorSystem.actorOf(new Props(new UntypedActorFactory() {
            @Override
            public UntypedActor create() throws Exception {
                return new GooglePageRankActor(1000, 2000);
            }
        }));

        actor.tell("facebook.com", null);
        actor.tell("vk.com", null);
        actor.tell("yahoo.com", null);
        actor.tell("techcrunch.com", null);
        actor.tell("abc.com", null);
        actor.tell("235ksfkdsfklsjrkl3j54.com", null);

        Thread.sleep(1000);
        actorSystem.shutdown();

    }

    /**
     * This actor gets Google Page Rank for a domain
     */
    static class GooglePageRankActor extends UntypedActor {

        final int connectionTimeoutMillis;
        final int readTimeoutMillis;
        final JenkinsHash jenkinsHash;

        public GooglePageRankActor(final int connectionTimeoutMillis, final int readTimeoutMillis) {
            this.connectionTimeoutMillis = connectionTimeoutMillis;
            this.readTimeoutMillis = readTimeoutMillis;
            jenkinsHash= new JenkinsHash();
        }

        @Override
        public void onReceive(Object message) throws Exception {
            if (message instanceof String) {
                int rank = getPR((String)message);
                System.out.println(message + " PageRank: " + rank);
            } else {
                unhandled(message);
            }
        }

        public int getPR(final String domain) {
            final String url = buildUrl(domain);

            int result = 0;

            try {
                final URLConnection conn = new URL(url).openConnection();
                conn.setConnectTimeout(connectionTimeoutMillis);
                conn.setReadTimeout(readTimeoutMillis);
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
            System.out.println(actorName + " started");
        }

        @Override
        public void postStop() {
            System.out.println(actorName + " stopped");
        }
    }
}
