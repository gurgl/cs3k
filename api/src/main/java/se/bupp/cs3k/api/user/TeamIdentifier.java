package se.bupp.cs3k.api.user;

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-01-27
 * Time: 22:51
 * To change this template use File | Settings | File Templates.
 */
public class TeamIdentifier {

    Long id;
    String name;

    public TeamIdentifier(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
