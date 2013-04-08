/*******************************************************************************
 * Copyright (c) 2010, 2013 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu;

import java.lang.annotation.Annotation;

/**
 * Implement this interface to {@link W}atch for {@link Q}ualified bean implementations of {@link T}.
 * <p>
 * The {@link Mediator} is responsible for translating updates to whatever the watchers expect. Mediation only occurs
 * when there are updates to qualified bindings and at least <i>one</i> live watcher instance. So if no-one requests or
 * injects an instance of the watcher then the mediator will not be called.
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
 * As soon as MyTabbedPane is instantiated Sisu uses the declared SwingTabMediator to send all known JPanels annotated
 * with @Tab to the watching MyTabbedPane. Sisu will continue to send updates, which add or remove tabs as appropriate,
 * until the MyTabbedPane instance becomes unreachable. Note how MyTabbedPane doesn't need to know about Sisu APIs and
 * vice-versa, SwingTabMediator takes care of the necessary interaction.
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
