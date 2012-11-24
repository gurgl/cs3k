package se.bupp.cs3k.api.score;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-11-14
 * Time: 23:56
 * To change this template use File | Settings | File Templates.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public interface ContestScore{
    public Map<Long, ? extends CompetitorScore> competitorScores();
    public CompetitorScore competitorScore(Long i);
    //public List<Long> competitorResults();
}
