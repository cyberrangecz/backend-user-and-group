package cz.cyberrange.platform.userandgroup.definition.annotations.swagger;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({METHOD, ANNOTATION_TYPE, TYPE})
@Retention(RUNTIME)
@ApiImplicitParams({
        @ApiImplicitParam(name = "page", dataType = "int", paramType = "query", value = "Results page you want to retrieve (0..N)", example = "0"),
        @ApiImplicitParam(name = "size", dataType = "int", paramType = "query", value = "Number of records per page.", example = "20"),
        @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query", value = "Sorting criteria in the format: property(asc|desc). "
                + "Default sort order is ascending. " + "Multiple sort criteria are supported.", example = "asc")})
public @interface ApiPageableSwagger {
}
