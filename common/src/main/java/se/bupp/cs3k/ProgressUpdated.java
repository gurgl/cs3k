package se.bupp.cs3k;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer;

import java.io.Serializable;


/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-11-01
 * Time: 05:09
 * To change this template use File | Settings | File Templates.
 */
public class ProgressUpdated implements Serializable {
    @TaggedFieldSerializer.Tag(1) public int progress;

    public static final long serialVersionUID = 103L;

    public ProgressUpdated(int progress) {
        this.progress = progress;
    }

    public ProgressUpdated() {
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }
}
