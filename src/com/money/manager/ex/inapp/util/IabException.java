/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package com.money.manager.ex.inapp.util;

/**
 * Exception thrown when something went wrong with in-app billing.
 * An IabException has an associated IabResult (an error).
 * To get the IAB result that caused this exception to be thrown,
 * call {@link #getResult()}.
 */
public class IabException extends Exception {
    IabResult mResult;

    public IabException(IabResult r) {
        this(r, null);
    }
    public IabException(int response, String message) {
        this(new IabResult(response, message));
    }
    public IabException(IabResult r, Exception cause) {
        super(r.getMessage(), cause);
        mResult = r;
    }
    public IabException(int response, String message, Exception cause) {
        this(new IabResult(response, message), cause);
    }

    /** Returns the IAB result (error) that this exception signals. */
    public IabResult getResult() { return mResult; }
}