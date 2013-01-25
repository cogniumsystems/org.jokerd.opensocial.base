package org.jokerd.opensocial.scheduler;

import org.jokerd.opensocial.api.model.ActivityEntry;
import org.jokerd.opensocial.cursors.IActivityCursor;
import org.jokerd.opensocial.cursors.StreamException;
import org.ubimix.commons.json.JsonArray;

/**
 * @author kotelnikov
 */
public class ActivitySerializerStream extends DelegateActivityCursor {

    private final JsonArray fArray = new JsonArray();

    public ActivitySerializerStream(IActivityCursor cursor) {
        super(cursor);
    }

    public JsonArray getArray() {
        return fArray;
    }

    @Override
    public boolean loadNext() throws StreamException {
        boolean result = super.loadNext();
        if (result) {
            ActivityEntry entry = getCurrent();
            fArray.addValue(entry);
        }
        return result;
    }

}