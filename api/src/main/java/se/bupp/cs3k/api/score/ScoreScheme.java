package se.bupp.cs3k.api.score;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-11-15
 * Time: 00:16
 * To change this template use File | Settings | File Templates.
 */
public interface ScoreScheme extends Serializable {

    String [] competitorTotalColHeaders();

    public interface CompetitorTotal extends Serializable {

        public Render getRenderer() ;

        interface Render {

            String [] render();
        }
    }

    interface ContestEvaluation {
        <T extends Number> String renderToHtml(ContestScore cs, Map<Long,T> competitorByAwardedPoints, NumberFormat nf);
    }

    interface CompetitorTotalEvaluation {



        public int compareField(CompetitorTotal a, CompetitorTotal b, int field);

        public CompetitorTotal calculateTotal(List<CompetitorScore> contestScores);
    }
}
