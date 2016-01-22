/*
 * dibs
 * Copyright (C) 2015 Humboldt-Universität zu Berlin
 * 
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this
 * program.  If not, see <http://www.gnu.org/licenses/>
 */

package university.dibs.dibs;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Collection of utilities.
 *
 * @author Sven Pfaller
 */
public class Util {
    /**
     * Prüft ob ein Wert in einem bestimmten Bereich liegt.
     *
     * @param value Wert, der geprüft werden soll
     * @param min Minimalwert (inklusive)
     * @param max Maximalwert (inklusive)
     * @return <code>true</code> wenn der Wert im definierten Bereich liegt, ansonsten
     *     <code>false</code>
     */
    public static <T extends Comparable<T>> boolean isInRange(T value, T min, T max) {
        return value.compareTo(min) >= 0 && value.compareTo(max) <= 0;
    }

    /**
     * Configures Java logging for dibs and sets the output format and verbosity.
     *
     * @param debug If <code>true</code>, will set the log level to <code>FINE</code> for dibs in
     * order to display debug messages
     */
    public static void configureLogging(boolean debug) {
        // Format: "$Time $Level $Logger: $Message[\n$Error]\n"
        System.setProperty("java.util.logging.SimpleFormatter.format",
            "%1$tT %4$s %3$s: %5$s%6$s%n");
        LogManager.getLogManager().reset();
        ConsoleHandler handler = new ConsoleHandler();
        // handler lets all messages through; output verbosity is regulated via Logger.setLevel()
        handler.setLevel(Level.FINEST);
        Logger.getLogger("").addHandler(handler);

        if (debug) {
            // limit debug logging to dibs
            Logger.getLogger("university.dibs.dibs").setLevel(Level.FINE);
        }
    }
}
