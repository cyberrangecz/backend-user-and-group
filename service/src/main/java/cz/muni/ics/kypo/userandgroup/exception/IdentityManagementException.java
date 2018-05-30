/*
 *  Project   : Cybernetic Proving Ground
 *
 *  Tool      : Identity Management Service
 *
 *  Author(s) : Filip Bogyai 395959@mail.muni.cz
 *
 *  Date      : 31.5.2016
 *
 *  (c) Copyright 2016 MASARYK UNIVERSITY
 *  All rights reserved.
 *
 *  This software is freely available for non-commercial use under license
 *  specified in following license agreement in LICENSE file. Please review the terms
 *  of the license agreement before using this software. If you are interested in
 *  using this software commercially orin ways not allowed in aforementioned
 *  license, feel free to contact Technology transfer office of the Masaryk university
 *  in order to negotiate ad-hoc license agreement.
 */
package cz.muni.ics.kypo.userandgroup.exception;

public class IdentityManagementException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public IdentityManagementException() {
        super();
    }

    public IdentityManagementException(String message) {
        super(message);
    }

    public IdentityManagementException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public IdentityManagementException(Throwable throwable) {
        super(throwable);
    }
}
