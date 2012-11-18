package se.bupp.cs3k.api.score;

import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-11-14
 * Time: 23:56
 * To change this template use File | Settings | File Templates.
 */
public interface ContestScore {
    public Map<Long, CompetitorScore> competitorScores();
    public List<Long> competitorResults();
}
