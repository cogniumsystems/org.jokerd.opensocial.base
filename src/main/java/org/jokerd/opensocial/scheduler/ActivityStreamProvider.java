package org.jokerd.opensocial.scheduler;

import org.jokerd.opensocial.api.model.DomainName;
import org.jokerd.opensocial.api.model.ObjectId;
import org.jokerd.opensocial.cursors.IActivityCursor;
import org.jokerd.opensocial.cursors.IActivityCursorProvider;
import org.jokerd.opensocial.cursors.StreamException;
import org.ubimix.commons.cursor.CompositeCursorProvider;

/**
 * @author kotelnikov
 */
public class ActivityStreamProvider
    extends
    CompositeCursorProvider<DomainName, ObjectId, StreamException, IActivityCursor>
    implements
    IActivityCursorProvider {

    public ActivityStreamProvider() {
        super();
    }

    public ActivityStreamProvider(IActivityCursorProvider defaultProvider) {
        super(defaultProvider);
    }

    @Override
    protected DomainName getKey(ObjectId parameter) {
        return parameter.getDomainName();
    }

}
