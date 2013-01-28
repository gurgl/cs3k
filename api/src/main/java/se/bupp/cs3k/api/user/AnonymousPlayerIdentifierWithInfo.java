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

    TeamIdentifier team;

    public AnonymousPlayerIdentifierWithInfo(String name, TeamIdentifier team) {
        super(name);
        this.team = team;
    }

    @Override
    public TeamIdentifier getTeam() {
        return team;
    }

}
