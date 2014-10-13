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

package org.eclipse.sisu.peaberry.util.ldap;

import java.util.Map;

import org.eclipse.sisu.peaberry.AttributeFilter;
import org.eclipse.sisu.peaberry.osgi.AttributeDictionary;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;

/**
 * Implementation of LDAP {@link AttributeFilter}, uses code from Apache Felix.
 * 
 * @author mcculls@gmail.com (Stuart McCulloch)
 */
public final class LdapAttributeFilter
    implements AttributeFilter {

  private final Filter filter;

  public LdapAttributeFilter(final String ldapFilter) {
    try {
      filter = FrameworkUtil.createFilter(ldapFilter);
    } catch (final InvalidSyntaxException e) {
      throw new IllegalArgumentException("Bad LDAP filter: " + ldapFilter);
    }
  }

  public boolean matches(final Map<String, ?> attributes) {
    return filter.match(null == attributes ? null : new AttributeDictionary(attributes));
  }

  @Override
  public String toString() {
    return filter.toString();
  }

  @Override
  public int hashCode() {
    return filter.hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof LdapAttributeFilter) {
      return filter.equals(((LdapAttributeFilter) obj).filter);
    }
    return false;
  }
}
