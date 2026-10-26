package cz.cyberrange.platform.userandgroup.rest.sorting;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import java.util.List;
import java.util.stream.Stream;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.web.method.HandlerMethod;

/**
 * Rewrites the {@code sort} query parameter of every paged operation that carries {@link
 * SortableProperties}: its values are enumerated as the allowed properties each in ascending and
 * descending form, and its description gives the format, the default order and the {@code id}
 * tiebreaker.
 */
public class SortParameterOpenApiCustomizer implements OperationCustomizer {

  private static final String SORT_PARAMETER_NAME = "sort";

  @Override
  public Operation customize(Operation operation, HandlerMethod handlerMethod) {
    SortableProperties sortableProperties =
        handlerMethod.getMethodAnnotation(SortableProperties.class);
    if (sortableProperties != null && operation.getParameters() != null) {
      operation.getParameters().stream()
          .filter(parameter -> SORT_PARAMETER_NAME.equals(parameter.getName()))
          .forEach(
              parameter ->
                  describeSortParameter(parameter, sortableProperties.value().properties()));
    }
    return operation;
  }

  private static void describeSortParameter(Parameter parameter, List<String> properties) {
    List<String> sortValues =
        properties.stream()
            .flatMap(property -> Stream.of(property + ",asc", property + ",desc"))
            .toList();
    StringSchema itemSchema = new StringSchema();
    itemSchema.setEnum(sortValues);
    parameter.setSchema(new ArraySchema().items(itemSchema));
    parameter.setDescription(
        ("Sort order as `property,(asc|desc)`; repeat the parameter to sort by several properties."
                + " Sortable properties: %s. Without this parameter rows are ordered by `id`"
                + " ascending. `id` ascending is always appended as the last sort key, so rows"
                + " with equal sort values keep one fixed order across pages. A property outside"
                + " the sortable properties answers HTTP 400.")
            .formatted(String.join(", ", properties)));
  }
}
