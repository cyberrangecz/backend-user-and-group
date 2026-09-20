package cz.cyberrange.platform.userandgroup.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Collections;
import java.util.List;

/**
 * Wraps one page of results together with the pagination metadata describing where that page sits
 * within the full result set. Returned by every paginated endpoint of the API.
 *
 * @param <E> type of the elements the page carries
 */
@Schema(description = "One page of results together with its place in the whole result set.")
public class PageResultResource<E> {

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  private List<E> content;

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  private Pagination pagination;

  public PageResultResource() {}

  public PageResultResource(List<E> content) {
    super();
    this.content = content;
  }

  public PageResultResource(List<E> content, Pagination pageMetadata) {
    super();
    this.content = content;
    this.pagination = pageMetadata;
  }

  /**
   * Returns the page's elements as an unmodifiable view.
   *
   * @return the page's elements
   * @throws NullPointerException when the content has not been set
   */
  public List<E> getContent() {
    return Collections.unmodifiableList(content);
  }

  public void setContent(List<E> content) {
    this.content = content;
  }

  public Pagination getPagination() {
    return pagination;
  }

  public void setPagination(Pagination pagination) {
    this.pagination = pagination;
  }

  @Override
  public String toString() {
    return "PageResultDTO [content="
        + content
        + ", pageMetadata="
        + pagination
        + ", getContent()="
        + getContent()
        + ", getPageMetadata()="
        + getPagination()
        + "]";
  }

  /** Describes a page's position, size and totals within its full result set. */
  public static class Pagination {

    @Schema(
        requiredMode = Schema.RequiredMode.REQUIRED,
        description = "Index of this page, counted from zero.",
        example = "1")
    private int number;

    @Schema(
        requiredMode = Schema.RequiredMode.REQUIRED,
        description = "How many elements this page holds.",
        example = "20")
    private int numberOfElements;

    @Schema(
        requiredMode = Schema.RequiredMode.REQUIRED,
        description = "Largest number of elements a page may hold.",
        example = "20")
    private int size;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private long totalElements;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    private int totalPages;

    public Pagination() {}

    public Pagination(
        int number, int numberOfElements, int size, long totalElements, int totalPages) {
      super();
      this.number = number;
      this.numberOfElements = numberOfElements;
      this.size = size;
      this.totalElements = totalElements;
      this.totalPages = totalPages;
    }

    public int getNumber() {
      return number;
    }

    public void setNumber(int number) {
      this.number = number;
    }

    public int getNumberOfElements() {
      return numberOfElements;
    }

    public void setNumberOfElements(int numberOfElements) {
      this.numberOfElements = numberOfElements;
    }

    public int getSize() {
      return size;
    }

    public void setSize(int size) {
      this.size = size;
    }

    public long getTotalElements() {
      return totalElements;
    }

    public void setTotalElements(long totalElements) {
      this.totalElements = totalElements;
    }

    public int getTotalPages() {
      return totalPages;
    }

    public void setTotalPages(int totalPages) {
      this.totalPages = totalPages;
    }

    @Override
    public String toString() {
      return "PageMetadata [number="
          + number
          + ", numberOfElements="
          + numberOfElements
          + ", size="
          + size
          + ", totalElements="
          + totalElements
          + ", totalPages="
          + totalPages
          + "]";
    }
  }
}
