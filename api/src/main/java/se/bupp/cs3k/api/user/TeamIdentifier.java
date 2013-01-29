package se.bupp.cs3k.api.user;

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-01-27
 * Time: 22:51
 * To change this template use File | Settings | File Templates.
 */
public class TeamIdentifier {

    Long reportableId;
    String name;

    public TeamIdentifier(Long reportableId, String name) {
        this.reportableId = reportableId;
        this.name = name;
    }

    public Long getReportableId() {
        return reportableId;
    }

    public String getName() {
        return name;
    }
}
