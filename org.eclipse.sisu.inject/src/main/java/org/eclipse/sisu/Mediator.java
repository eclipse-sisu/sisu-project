/*
 * Copyright (c) 2010-2024 Sonatype, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 */
package org.eclipse.sisu;

import java.lang.annotation.Annotation;

/**
 * <b>W</b>atches for <b>Q</b>ualified bean implementations of <b>T</b>:<br>
 * <br>
 * 
 * <pre>
 * // add @Named for automatic registration
 * public class MyMediator
 *     implements Mediator&lt;Named, MyType, MyWatcher&gt;
 * {
 *     public void add( BeanEntry&lt;Named, MyType&gt; entry, MyWatcher watcher )
 *         throws Exception
 *     {
 *         // translate event to whatever the watcher expects
 *     }
 * 
 *     public void remove( BeanEntry&lt;Named, MyType&gt; entry, MyWatcher watcher )
 *         throws Exception
 *     {
 *         // translate event to whatever the watcher expects
 *     }
 * }
 * </pre>
 * 
 * Mediator implementations must have a public no-arg constructor; they are neither injected nor injectable, acting
 * instead as stateless translators.
 * <p>
 * IMPORTANT: mediation occurs when bindings change and there is at least <b>one</b> live watcher. If no-one requests or
 * injects an instance of the watcher type then the mediator will <b>not</b> be called.
 * <p>
 * In the following example as soon as MyTabbedPane is injected, Sisu will use the SwingTabMediator to deliver all known
 * JPanels annotated with @Tab to the watching MyTabbedPane. Sisu will continue to send updates, which add or remove
 * tabs as appropriate, until the MyTabbedPane instance becomes unreachable. MyTabbedPane doesn't need to know anything
 * about Sisu APIs and vice-versa because SwingTabMediator takes care of the necessary translation.
 * 
 * <pre>
 * &#064;Named
 * public class MyTabbedPane
 *     extends JTabbedPane
 * {
 *     // watcher
 * }
 * 
 * &#064;Qualifier
 * &#064;Retention( RetentionPolicy.RUNTIME )
 * public @interface Tab
 * {
 *     String title();
 * }
 * 
 * &#064;Tab( title = &quot;Summary&quot; )
 * public class SummaryTab
 *     extends JPanel
 * {
 *     // qualified bean
 * }
 * 
 * &#064;Tab( title = &quot;Notes&quot; )
 * public class NotesTab
 *     extends JPanel
 * {
 *     // qualified bean
 * }
 * 
 * &#064;Named
 * public class SwingTabMediator
 *     implements Mediator&lt;Tab, JPanel, MyTabbedPane&gt;
 * {
 *     public void add( BeanEntry&lt;Tab, JPanel&gt; entry, final MyTabbedPane watcher )
 *         throws Exception
 *     {
 *         final Tab tab = entry.getKey();
 *         final JPanel panel = entry.getValue();
 * 
 *         SwingUtilities.invokeLater( new Runnable()
 *         {
 *             public void run()
 *             {
 *                 watcher.addTab( tab.title(), panel );
 *             }
 *         } );
 *     }
 * 
 *     public void remove( BeanEntry&lt;Tab, JPanel&gt; entry, final MyTabbedPane watcher )
 *         throws Exception
 *     {
 *         final Tab tab = entry.getKey();
 * 
 *         SwingUtilities.invokeLater( new Runnable()
 *         {
 *             public void run()
 *             {
 *                 watcher.removeTabAt( watcher.indexOfTab( tab.title() ) );
 *             }
 *         } );
 *     }
 * }
 * </pre>
 * 
 * @see org.eclipse.sisu.inject.BeanLocator
 */
public interface Mediator<Q extends Annotation, T, W>
{
    /**
     * Processes the added {@link BeanEntry} and sends the necessary updates to the watcher.
     * 
     * @param entry The added bean entry
     * @param watcher The watching object
     */
    void add( BeanEntry<Q, T> entry, W watcher )
        throws Exception;

    /**
     * Processes the removed {@link BeanEntry} and sends the necessary updates to the watcher.
     * 
     * @param entry The removed bean entry
     * @param watcher The watching object
     */
    void remove( BeanEntry<Q, T> entry, W watcher )
        throws Exception;
}
