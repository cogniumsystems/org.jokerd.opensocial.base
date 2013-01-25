/**
 * 
 */
package org.jokerd.opensocial.cursors;

import java.util.HashMap;
import java.util.Map;

import org.jokerd.opensocial.api.model.DomainName;
import org.jokerd.opensocial.api.model.ObjectId;

/**
 * This cursor provider dispatches real cursor creation to registered providers
 * associated with specific domain names.
 * 
 * @author kotelnikov
 */
public class CompositeActivityCursorProvider implements IActivityCursorProvider {

    /**
     * 
     */
    private final Map<DomainName, IActivityCursorProvider> fMap = new HashMap<DomainName, IActivityCursorProvider>();

    /**
     * 
     */
    public CompositeActivityCursorProvider() {
    }

    /**
     * @see org.jokerd.opensocial.cursors.IActivityCursorProvider#getCursor(org.jokerd.opensocial.api.model.ObjectId)
     */
    @Override
    public IActivityCursor getCursor(ObjectId streamId) throws StreamException {
        DomainName domainName = streamId.getDomainName();
        IActivityCursorProvider provider = getCursorProvider(domainName);
        if (provider == null) {
            throw new IllegalArgumentException(
                "There is no stream provider found "
                    + "for the specified identifier. "
                    + "Stream ID: "
                    + streamId);
        }
        return provider.getCursor(streamId);
    }

    /**
     * Returns a cursor provider associated with the specified domain name.
     * 
     * @param domainName the domain name of the cursor to return
     * @return a cursor provider registered for the specified domain name
     */
    public IActivityCursorProvider getCursorProvider(DomainName domainName) {
        synchronized (fMap) {
            return fMap.get(domainName);
        }
    }

    /**
     * Removes a cursor provider associated with the given domain name.
     * 
     * @param domainName the domain name specific for the removed cursor
     *        provider
     */
    public void removeCursorProvider(DomainName domainName) {
        synchronized (fMap) {
            fMap.remove(domainName);
        }
    }

    /**
     * Registers a new cursor provider specific for the given domain name.
     * 
     * @param domainName the name of domain specific for the given cursor
     *        provider
     * @param cursorProvider the cursor provider associated with the given
     *        domain name
     */
    public void setCursorProvider(
        DomainName domainName,
        IActivityCursorProvider cursorProvider) {
        synchronized (fMap) {
            fMap.put(domainName, cursorProvider);
        }
    }

}
