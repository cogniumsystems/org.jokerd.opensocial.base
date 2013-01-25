package org.jokerd.opensocial.scheduler;

import org.jokerd.opensocial.api.model.ActivityEntry;
import org.jokerd.opensocial.cursors.IActivityCursor;
import org.jokerd.opensocial.cursors.StreamException;

/**
 * @author kotelnikov
 */
public class DelegateActivityCursor implements IActivityCursor {

    private IActivityCursor fCursor;

    protected DelegateActivityCursor() {
    }

    public DelegateActivityCursor(IActivityCursor cursor) {
        setCursor(cursor);
    }

    @Override
    public void close() throws StreamException {
        fCursor.close();
    }

    @Override
    public ActivityEntry getCurrent() {
        return fCursor.getCurrent();
    }

    public IActivityCursor getCursor() {
        return fCursor;
    }

    @Override
    public boolean loadNext() throws StreamException {
        boolean result = fCursor.loadNext();
        return result;
    }

    protected void setCursor(IActivityCursor cursor) {
        fCursor = cursor;
    }

}