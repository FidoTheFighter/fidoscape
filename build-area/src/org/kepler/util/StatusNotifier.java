/* A class to notify listeners of status messages.
 * 
 * Copyright (c) 2014 The Regents of the University of California.
 * All rights reserved.
 *
 * '$Author: crawl $'
 * '$Date: 2014-08-13 15:54:36 -0700 (Wed, 13 Aug 2014) $' 
 * '$Revision: 32874 $'
 * 
 * Permission is hereby granted, without written agreement and without
 * license or royalty fees, to use, copy, modify, and distribute this
 * software and its documentation for any purpose, provided that the above
 * copyright notice and the following two paragraphs appear in all copies
 * of this software.
 *
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 * FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 * ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 * THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 * PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 * CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 * ENHANCEMENTS, OR MODIFICATIONS.
 *
 */
package org.kepler.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/** A class to notify listeners of status messages.
 * 
 *  @author Daniel Crawl
 *  @version $Id: StatusNotifier.java 32874 2014-08-13 22:54:36Z crawl $
 */
public class StatusNotifier {

    /** Add a StatusListener to be notified of messages. */
    public static void addStatusListener(StatusListener listener) {
    	_statusListeners.add(listener);
    }

    /** Clear any listeners. */
    public static void clearStatusListeners() {
    	_statusListeners.clear();
    }
    
    /** Log a status message and any notify listeners.
     *  @param message A message describing status.  
     */
    public static void log(String message) {
        for(StatusListener startupListener : _statusListeners) {
            startupListener.log(message);
        }
    }
    
    /** Remove a StatusListener. */
    public static void removeStatusListener(StatusListener listener) {
    	_statusListeners.remove(listener);
    }
    
    /** A list of startup listeners. */
    private static Set<StatusListener> _statusListeners =
        Collections.synchronizedSet(new HashSet<StatusListener>());
}