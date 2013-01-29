package se.bupp.cs3k.api.user;

import se.bupp.cs3k.api.SimplePlayerInfo;

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-01-27
 * Time: 22:46
 * To change this template use File | Settings | File Templates.
 */
public class AnonymousPlayerIdentifierWithInfo extends AnonymousPlayerIdentifier implements SimplePlayerInfo {

    Long reportableId;
    TeamIdentifier team;

    public AnonymousPlayerIdentifierWithInfo(String name, Long reportableId, TeamIdentifier team) {
        super(name);
        this.reportableId = reportableId;
        this.team = team;
    }

    public Long getReportableId() {
        return reportableId;
    }

    @Override
    public TeamIdentifier getTeam() {
        return team;
    }

}
