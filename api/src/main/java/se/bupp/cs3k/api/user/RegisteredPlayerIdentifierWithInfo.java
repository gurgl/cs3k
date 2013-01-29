package se.bupp.cs3k.api.user;

import se.bupp.cs3k.api.SimplePlayerInfo;
import se.bupp.cs3k.api.user.RegisteredPlayerIdentifier;

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-10-19
 * Time: 21:22
 * To change this template use File | Settings | File Templates.
 */
public class RegisteredPlayerIdentifierWithInfo extends RegisteredPlayerIdentifier implements SimplePlayerInfo {
    String name;
    Long reportableId;
    TeamIdentifier team;

    public RegisteredPlayerIdentifierWithInfo(Long userId, String name, Long reportableId, TeamIdentifier team) {
        super(userId);
        this.name = name;
        this.reportableId = reportableId;
        this.team = team;
    }

    public Long getReportableId() {
        return reportableId;
    }

    public String getName() {
        return name;
    }

    @Override
    public TeamIdentifier getTeam() {
        return team;
    }
}


