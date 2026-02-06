/*******************************************************************************
 * Copyright (c) 2008, 2014 Stuart McCulloch
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stuart McCulloch - initial API and implementation
 *******************************************************************************/

package org.eclipse.sisu.peaberry.util;

import java.util.Map;

import org.eclipse.sisu.peaberry.AttributeFilter;
import org.eclipse.sisu.peaberry.util.ldap.LdapAttributeFilter;

/**
 * Methods for dealing with {@link AttributeFilter}s.
 * 
 * @author mcculls@gmail.com (Stuart McCulloch)
 */
public final class Filters {

  // instances not allowed
  private Filters() {}

  /**
   * Create an {@link AttributeFilter} based on the given LDAP filter string.
   * 
   * @param ldapFilter RFC-1960 LDAP filter
   * @return service attribute filter
   * 
   * @see <a href="http://www.ietf.org/rfc/rfc1960.txt">RFC-1960</a>
   */
  public static AttributeFilter ldap(final String ldapFilter) {
    return new LdapAttributeFilter(ldapFilter);
  }

  /**
   * Create an <i>objectClass</i> {@link AttributeFilter} from the given API.
   * 
   * @param interfaces service API
   * @return service attribute filter
   */
  public static AttributeFilter objectClass(final Class<?>... interfaces) {
    final StringBuilder filter = new StringBuilder();
    int numClauses = 0;

    for (final Class<?> i : interfaces) {
      if (null != i && Object.class != i) { // NOSONAR
        filter.append("(objectClass=").append(i.getName()).append(')');
        numClauses++;
      }
    }

    if (0 == numClauses) {
      return null;
    } else if (1 < numClauses) {
      filter.insert(0, "(&").append(')');
    }

    return new LdapAttributeFilter(filter.toString());
  }

  /**
   * Create an {@link AttributeFilter} based on the given service attributes.
   * 
   * @param sampleAttributes sample attributes
   * @return sample attribute filter
   */
  public static AttributeFilter attributes(final Map<String, ?> sampleAttributes) {
    return new AttributeFilter() {
      public boolean matches(final Map<String, ?> attributes) {
        return null != attributes && attributes.entrySet().containsAll(sampleAttributes.entrySet());
      }
    };
  }
}
