package pl.edu.agh.twitter;


import org.jboss.weld.environment.se.Weld;
import pl.edu.agh.twitter.sentiment.Sentiment140Reader;

public class EntryPoint {
    public static void main(String[] args) {
        Weld weld = new Weld();
        run(Sentiment140Reader.class, weld);
        weld.shutdown();
    }

    private static void run(Class<? extends Startable> itemClass, Weld weld) {
        weld.initialize().instance().select(itemClass).get().start();
    }

}
