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
package org.eclipse.sisu.inject;

import java.util.Map;
import java.util.logging.Level;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.ProvisionException;
import com.google.inject.spi.Element;
import com.google.inject.spi.Elements;

/**
 * Utility methods for dealing with container logging and recovery.
 * <p>
 * Set <b>-Dsisu.debug</b> to send detailed tracing to the console.
 */
public final class Logs
{
    // ----------------------------------------------------------------------
    // Static initialization
    // ----------------------------------------------------------------------

    static
    {
        String newLine;
        boolean toConsole;
        try
        {
            newLine = System.getProperty( "line.separator", "\n" );
            final String debug = System.getProperty( "sisu.debug", "false" );
            toConsole = "".equals( debug ) || "true".equalsIgnoreCase( debug );
        }
        catch ( final RuntimeException e )
        {
            newLine = "\n";
            toConsole = false;
        }
        NEW_LINE = newLine;
        Sink sink;
        try
        {
            sink = toConsole ? new ConsoleSink() : new SLF4JSink();
        }
        catch ( final RuntimeException e )
        {
            sink = new JULSink();
        }
        catch ( final LinkageError e )
        {
            sink = new JULSink();
        }
        SINK = sink;
    }

    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    public static final String NEW_LINE;

    private static final String SISU = "Sisu";

    private static final Sink SINK;

    public static final boolean TRACE_ENABLED = SINK.isTraceEnabled();

    public static final boolean DEBUG_ENABLED = SINK.isDebugEnabled();

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    private Logs()
    {
        // static utility class, not allowed to create instances
    }

    // ----------------------------------------------------------------------
    // Utility methods
    // ----------------------------------------------------------------------

    /**
     * Logs a trace message; uses "{}" format anchors. Pass {@link Throwable}s in last parameter for special handling.
     *
     * @param format The trace message format
     * @param arg1 First object to format
     * @param arg2 Second object to format
     */
    public static void trace( final String format, final Object arg1, final Object arg2 )
    {
        if ( TRACE_ENABLED )
        {
            SINK.trace( format( format( format, arg1 ), arg2 ), arg2 instanceof Throwable ? (Throwable) arg2 : null );
        }
    }

    /**
     * Logs a debug message; uses "{}" format anchors. Pass {@link Throwable}s in last parameter for special handling.
     *
     * @param format The trace message format
     * @param arg1 First object to format
     * @param arg2 Second object to format
     */
    public static void debug( final String format, final Object arg1, final Object arg2 )
    {
        if ( DEBUG_ENABLED )
        {
            SINK.debug( format( format( format, arg1 ), arg2 ), arg2 instanceof Throwable ? (Throwable) arg2 : null );
        }
    }

    /**
     * Logs a warning message; uses "{}" format anchors. Pass {@link Throwable}s in last parameter for special handling.
     * 
     * @param format The warning message format
     * @param arg1 First object to format
     * @param arg2 Second object to format
     */
    public static void warn( final String format, final Object arg1, final Object arg2 )
    {
        SINK.warn( format( format( format, arg1 ), arg2 ), arg2 instanceof Throwable ? (Throwable) arg2 : null );
    }

    /**
     * Helper method for catching {@link Throwable}s; severe errors such as {@link ThreadDeath} are always re-thrown.
     * 
     * @param problem The problem
     */
    public static void catchThrowable( final Throwable problem )
    {
        for ( Throwable cause = problem; cause != null; cause = cause.getCause() )
        {
            if ( cause instanceof ThreadDeath || cause instanceof VirtualMachineError )
            {
                throw (Error) cause; // must immediately re-throw severe errors
            }
        }
    }

    /**
     * Helper method for throwing {@link Throwable}s; checked exceptions are wrapped as {@link ProvisionException}s.
     * 
     * @param problem The problem
     */
    public static void throwUnchecked( final Throwable problem )
    {
        if ( problem instanceof RuntimeException )
        {
            throw (RuntimeException) problem;
        }
        if ( problem instanceof Error )
        {
            throw (Error) problem;
        }
        // this cast lets us load the 'Logs' class and log messages even if Guice is not available
        throw RuntimeException.class.cast( new ProvisionException( problem.toString(), problem ) );
    }

    /**
     * Returns an identity string for the given object.
     * 
     * @see System#identityHashCode(Object)
     * @param object The object
     * @return Identity string of the object.
     */
    public static String identityToString( final Object object )
    {
        return null == object ? null : object.getClass().getName() + '@' //
            + Integer.toHexString( System.identityHashCode( object ) );
    }

    /**
     * Returns a string representation of the given {@link Module}.
     * 
     * @param module The module
     * @return String representation of the module.
     */
    public static String toString( final Module module )
    {
        final StringBuilder buf = new StringBuilder( identityToString( module ) );
        buf.append( NEW_LINE ).append( NEW_LINE );
        buf.append( "-----[elements]----------------------------------------------------------------" ).append( NEW_LINE );
        int i = 0;
        for ( final Element e : Elements.getElements( module ) )
        {
            buf.append( i++ ).append( ". " ).append( e ).append( NEW_LINE );
        }
        return buf.append( "-------------------------------------------------------------------------------" ).append( NEW_LINE ).toString();
    }

    /**
     * Returns a string representation of the given {@link Injector}.
     * 
     * @param injector The injector
     * @return String representation of the injector.
     */
    public static String toString( final Injector injector )
    {
        final StringBuilder buf = new StringBuilder( identityToString( injector ) );
        if ( null != injector.getParent() ) // NOSONAR
        {
            buf.append( " parent: " ).append( identityToString( injector.getParent() ) );
        }
        buf.append( NEW_LINE ).append( NEW_LINE );
        buf.append( "-----[explicit bindings]-------------------------------------------------------" ).append( NEW_LINE );
        int i = 0;
        final Map<Key<?>, Binding<?>> explicitBindings = injector.getBindings();
        for ( final Binding<?> b : explicitBindings.values() )
        {
            buf.append( i++ ).append( ". " ).append( b ).append( NEW_LINE );
        }
        buf.append( "-----[implicit bindings]-------------------------------------------------------" ).append( NEW_LINE );
        for ( final Binding<?> b : injector.getAllBindings().values() )
        {
            if ( !explicitBindings.containsKey( b.getKey() ) )
            {
                buf.append( i++ ).append( ". " ).append( b ).append( NEW_LINE );
            }
        }
        return buf.append( "-------------------------------------------------------------------------------" ).append( NEW_LINE ).toString();
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Replaces the first available formatting anchor with the given object.
     * 
     * @param format The format string
     * @param arg The object to format
     */
    private static String format( final String format, final Object arg )
    {
        final int len = format.length();
        boolean detailed = true;
        int cursor = 0;
        for ( char prevChar = ' ', currChar; cursor < len; prevChar = currChar, cursor++ )
        {
            currChar = format.charAt( cursor );
            if ( prevChar == '{' && currChar == '}' )
            {
                break; // replace anchor with String.valueOf
            }
            if ( prevChar == '<' && currChar == '>' )
            {
                detailed = false;
                break; // use Logs.identityToString instead
            }
        }
        if ( cursor >= len )
        {
            return format;
        }
        final StringBuilder buf = new StringBuilder();
        if ( --cursor > 0 )
        {
            buf.append( format.substring( 0, cursor ) );
        }
        try
        {
            buf.append( detailed ? arg : identityToString( arg ) );
        }
        catch ( final RuntimeException e )
        {
            buf.append( e );
        }
        cursor += 2;
        if ( cursor < len )
        {
            buf.append( format.substring( cursor, len ) );
        }
        return buf.toString();
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    /**
     * Something that accepts formatted messages.
     */
    private interface Sink
    {
        /**
         * @return {@code true} if trace is enabled; otherwise {@code false}
         */
        boolean isTraceEnabled();

        /**
         * @return {@code true} if debug is enabled; otherwise {@code false}
         */
        boolean isDebugEnabled();

        /**
         * Accepts a trace message and optional exception cause.
         *
         * @param message The trace message
         * @param cause The exception cause
         */
        void trace( String message, Throwable cause );

        /**
         * Accepts a debug message and optional exception cause.
         *
         * @param message The debug message
         * @param cause The exception cause
         */
        void debug( String message, Throwable cause );

        /**
         * Accepts a warning message and optional exception cause.
         * 
         * @param message The warning message
         * @param cause The exception cause
         */
        void warn( String message, Throwable cause );
    }

    /**
     * {@link Sink}s messages to the system console.
     */
    static final class ConsoleSink
        implements Sink
    {
        // ----------------------------------------------------------------------
        // Constants
        // ----------------------------------------------------------------------

        private static final String TRACE = "TRACE: " + SISU + " - ";

        private static final String DEBUG = "DEBUG: " + SISU + " - ";

        private static final String WARN = "WARN: " + SISU + " - ";

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        public boolean isTraceEnabled()
        {
            return true;
        }

        public boolean isDebugEnabled()
        {
            return true;
        }

        public void trace( final String message, final Throwable cause )
        {
            System.out.println( TRACE + message );
            if ( null != cause )
            {
                cause.printStackTrace( System.out );
            }
        }

        public void debug( final String message, final Throwable cause )
        {
            System.out.println( DEBUG + message );
            if ( null != cause )
            {
                cause.printStackTrace( System.out );
            }
        }

        public void warn( final String message, final Throwable cause )
        {
            System.err.println( WARN + message );
            if ( null != cause )
            {
                cause.printStackTrace( System.err );
            }
        }
    }

    /**
     * {@link Sink}s messages to the JDK.
     */
    static final class JULSink
        implements Sink
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger( SISU );

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        public boolean isTraceEnabled()
        {
            return logger.isLoggable( Level.FINER );
        }

        public boolean isDebugEnabled()
        {
            return logger.isLoggable( Level.FINE );
        }

        public void trace( final String message, final Throwable cause )
        {
            logger.log( Level.FINER, message, cause );
        }

        public void debug( final String message, final Throwable cause )
        {
            logger.log( Level.FINE, message, cause );
        }

        public void warn( final String message, final Throwable cause )
        {
            logger.log( Level.WARNING, message, cause );
        }
    }

    /**
     * {@link Sink}s messages via SLF4J.
     */
    static final class SLF4JSink
        implements Sink
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger( SISU );

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        public boolean isTraceEnabled()
        {
            return logger.isTraceEnabled();
        }

        public boolean isDebugEnabled()
        {
            return logger.isDebugEnabled();
        }

        public void trace( final String message, final Throwable cause )
        {
            logger.trace( message, cause );
        }

        public void debug( final String message, final Throwable cause )
        {
            logger.debug( message, cause );
        }

        public void warn( final String message, final Throwable cause )
        {
            logger.warn( message, cause );
        }
    }
}
