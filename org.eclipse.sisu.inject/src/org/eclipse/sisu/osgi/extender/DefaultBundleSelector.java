package org.eclipse.sisu.osgi.extender;

import java.util.Dictionary;

import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;

/**
 * Default implementation of {@link BundleSelector}. {@link DefaultBundleSelector} selects a bundle if such bundle
 * imports package <b>javax.inject</b> or <b>com.google.inject</b>
 */
public class DefaultBundleSelector
    implements BundleSelector
{

    /*
     * (non-Javadoc)
     * @see org.eclipse.sisu.osgi.extender.BundleSelector#select(org.osgi.framework.Bundle)
     */
    public boolean select( Bundle bundle )
    {
        final Dictionary<?, ?> headers = bundle.getHeaders();
        final String host = (String) headers.get( Constants.FRAGMENT_HOST );
        if ( null != host )
        {
            return false; // fragment, we'll scan it when we process the host
        }
        final String imports = (String) headers.get( Constants.IMPORT_PACKAGE );
        if ( null == imports )
        {
            return false; // doesn't import any interesting injection packages
        }
        return imports.contains( "javax.inject" ) || imports.contains( "com.google.inject" );
    }

}
