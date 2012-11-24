package se.bupp.cs3k.api.score;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-11-15
 * Time: 00:16
 * To change this template use File | Settings | File Templates.
 */
public interface ScoreScheme extends Serializable {

    public Class<? extends ContestScore> getContestStoreClass();

    String [] competitorTotalColHeaders();
    String renderToHtml(ContestScore cs, Set<Long> competitors);

    public interface CompetitorTotal extends Serializable {

        public Render getRenderer() ;

        interface Render {

            String [] render();
        }
    }

    public CompetitorTotal calculateTotal(List<CompetitorScore> contestScores);
    public int compareField(CompetitorTotal a, CompetitorTotal b, int field);
}
