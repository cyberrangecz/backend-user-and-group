package cz.muni.ics.kypo.userandgroup.rest;

import cz.muni.ics.kypo.userandgroup.api.exceptions.UserAndGroupFacadeException;
import cz.muni.ics.kypo.userandgroup.exceptions.UserAndGroupServiceException;
import cz.muni.ics.kypo.userandgroup.rest.exceptions.ConflictException;
import cz.muni.ics.kypo.userandgroup.rest.exceptions.ResourceNotCreatedException;
import cz.muni.ics.kypo.userandgroup.rest.exceptions.ResourceNotFoundException;
import org.springframework.security.access.AccessDeniedException;

import javax.ws.rs.InternalServerErrorException;


public class ExceptionSorter {
    private ExceptionSorter() {
        throw new IllegalStateException("Util class");
    }

    public static RuntimeException throwException(UserAndGroupFacadeException ex) {
        if (ex.getCause() instanceof UserAndGroupServiceException) {
            switch (((UserAndGroupServiceException) ex.getCause()).getCode()) {
                case RESOURCE_NOT_FOUND:
                    return new ResourceNotFoundException(ex);
                case RESOURCE_CONFLICT:
                    return new ConflictException(ex);
                case RESOURCE_NOT_CREATED:
                    return new ResourceNotCreatedException(ex);
                case SECURITY_RIGHTS:
                    return new AccessDeniedException("Access is denied.");
                case UNEXPECTED_ERROR:
                default:
                    return new InternalServerErrorException(ex);
            }
        } else {
            return new InternalServerErrorException(ex);
        }

    }

}

