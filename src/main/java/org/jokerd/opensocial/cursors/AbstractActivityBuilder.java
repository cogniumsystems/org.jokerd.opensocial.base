/**
 * 
 */
package org.jokerd.opensocial.cursors;

import org.jokerd.opensocial.api.model.DomainName;
import org.jokerd.opensocial.api.model.ObjectId;
import org.ubimix.commons.digests.Sha1Digest;

/**
 * @author kotelnikov
 */
public abstract class AbstractActivityBuilder {

    private final DomainName HASH = new DomainName("hash");

    /**
     * 
     */
    public AbstractActivityBuilder() {
    }

    protected ObjectId generateId(String token) {
        String uri = getGeneratedIdBase();
        DomainName domain = getGeneratedIdDomain();
        String str = token != null ? uri + ";" + token : uri + "";
        Sha1Digest digest = Sha1Digest.builder().update(str).build();
        return new ObjectId(domain, "sha1-" + digest.toString());
    }

    protected abstract DomainName getDomainName();

    protected String getGeneratedIdBase() {
        return "";
    }

    protected DomainName getGeneratedIdDomain() {
        return HASH;
    }

    protected ObjectId getId(String localId) {
        return new ObjectId(getDomainName(), localId);
    }

    protected ObjectId getId(String type, String localId) {
        if (type != null && !"".equals(type)) {
            localId = type + "-" + localId;
        }
        return getId(localId);
    }
}
