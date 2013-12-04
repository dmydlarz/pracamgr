package pl.edu.agh.twitter;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import pl.edu.agh.twitter.crawler.TwitterServiceProvider;
import pl.edu.agh.twitter.model.MatchEvent;
import twitter4j.*;

import java.util.List;

public class Main {
    private static TwitterStream twitterStream = TwitterServiceProvider.getTwitterStream();
    private static final String[] LANGUAGES = new String[]{"en"};
    private static Logger logger = Logger.getLogger(Main.class);
    private static TwitterDAO twitterDAO = new TwitterDAO();

    public static void main(String[] args) {
        MatchEvent match1 = findMatchEvent("arsenal", "hull");
        MatchEvent match2 = findMatchEvent("manchester united", "everton");
        MatchEvent match3 = findMatchEvent("sunderland", "chelsea");
        MatchEvent match4 = findMatchEvent("west bromwich", "manchester city");
//        logger.info(match4.getKeywords());
        consume(match1, match2, match3, match4);
    }

    public static MatchEvent findMatchEvent(String homeTeam, String awayTeam) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        final String queryString = "FROM MatchEvent e WHERE lower(e.homeTeam.name) LIKE :homeTeam AND lower(e.awayTeam.name) LIKE :awayTeam";
        Query query = session.createQuery(queryString);
        final List items = query.setParameter("homeTeam", "%" + homeTeam.toLowerCase() + "%").setParameter("awayTeam", "%" + awayTeam.toLowerCase() + "%").list();
        MatchEvent matchEvent = (MatchEvent) items.get(0);
        session.close();
        return matchEvent;
    }

    private static FilterQuery filterQuery(MatchEvent ... matchEvents) {
        final FilterQuery query = new FilterQuery();
        query.language(LANGUAGES);
        query.track(getKeywords(matchEvents));
        return query;
    }

    private static void consume(MatchEvent ... matchEvents) {
        twitterStream.addListener(new MyStatusListener(matchEvents));
        twitterStream.filter(filterQuery(matchEvents));
    }

    private static String[] getKeywords(MatchEvent[] matchEvents) {
        List<String> keywords = Lists.newArrayList();
        for(MatchEvent matchEvent : matchEvents) {
            keywords.addAll(matchEvent.getKeywords());
        }
        return Iterables.toArray(keywords, String.class);
    }

    public static class MyStatusListener implements StatusListener {
        private MatchEvent[] matchEvents;

        MyStatusListener(MatchEvent[] matchEvents) {
            this.matchEvents = matchEvents;
        }

        public void onStatus(Status status) {
            twitterDAO.parseAndPersistTweet(matchEvents, status);
        }

        public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
        }

        public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
        }

        public void onException(Exception ex) {
            ex.printStackTrace();
        }

        @Override
        public void onScrubGeo(long arg0, long arg1) {
        }

        @Override
        public void onStallWarning(StallWarning arg0) {
        }
    }

    ;
}
