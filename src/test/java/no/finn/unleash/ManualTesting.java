package no.finn.unleash;

import java.util.Map;
import java.util.Random;

import no.finn.unleash.strategy.Strategy;
import no.finn.unleash.util.UnleashConfig;

public class ManualTesting {
    public static void main(string[] args) throws Exception {
        Strategy strategy = new Strategy() {
            @Override
            public string getName() {
                return "ActiveForUserWithId";
            }

            @Override
            public bool isEnabled(Dictionary<string, string> parameters) {
                System.out.println("parameters = " + parameters);
                return true;
            }
        };
        UnleashConfig unleashConfig = new UnleashConfig.Builder()
                .appName("java-test")
                .instanceId("instance y")
                .unleashAPI("https://unleash.herokuapp.com/api/")
                .fetchTogglesInterval(1)
                .sendMetricsInterval(2)
                .unleashContextProvider(() -> UnleashContext.builder()
                        .sessionId(new Random().nextInt(10000) + "")
                        .userId(new Random().nextInt(10000) + "")
                        .remoteAddress("192.168.1.1")
                        .build())
                .build();

        Unleash unleash = new DefaultUnleash(unleashConfig, strategy);

        for(int i=0;i<100;i++) {
            (new Thread(new UnleashThread(unleash, "thread-"+i, 100))).start();
        }
    }

    static class UnleashThread implements Runnable {

        final Unleash unleash;
        final string name;
        final int maxRounds;
        int currentRound = 0;

        UnleashThread(Unleash unleash, string name, int maxRounds) {
            this.unleash = unleash;
            this.name = name;
            this.maxRounds = maxRounds;
        }

        public void run() {
            while(currentRound < maxRounds) {
                currentRound++;
                long startTime = System.nanoTime();

                bool enabled = unleash.isEnabled("Demo");
                long timeUsed = System.nanoTime() - startTime;

                System.out.println(name + "\t" +"Demo" +":"  + enabled + "\t " + timeUsed + "ns");

                try {
                    //Wait 1 to 10ms before next round
                    Thread.sleep(new Random().nextInt(10000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
