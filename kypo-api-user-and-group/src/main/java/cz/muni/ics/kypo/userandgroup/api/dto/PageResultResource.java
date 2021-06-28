package cz.muni.ics.kypo.userandgroup.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Collections;
import java.util.List;

/**
 * This class is used to replace Page class and reduce the number of returned elements (standard Page class contains
 * fields, which are not useful (backward compatibility)).
 */
@ApiModel(value = "Result info (Page)",
        description = "Content (Retrieved data) and meta information about REST API result page. Including page number, number of elements in page, size of elements, total number of elements and total number of pages")
public class PageResultResource<E> {

    @ApiModelProperty(value = "Content - (Retrieved data) from databases.", required = true, position = 1)
    private List<E> content;
    @ApiModelProperty(value = "Pagination including: page number, number of elements in page, size, total elements and total pages.", required = true, position = 2)
    private Pagination pagination;

    public PageResultResource() {
    }

    public PageResultResource(List<E> content) {
        super();
        this.content = content;
    }

    public PageResultResource(List<E> content, Pagination pageMetadata) {
        super();
        this.content = content;
        this.pagination = pageMetadata;
    }

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

    public static class Pagination {

        @ApiModelProperty(value = "Page number.", required = true, example = "1")
        private int number;
        @ApiModelProperty(value = "Number of elements in page.", required = true, example = "20")
        private int numberOfElements;
        @ApiModelProperty(value = "Page size.", required = true, example = "20")
        private int size;
        @ApiModelProperty(value = "Total number of elements in this resource (in all Pages).", required = true, example = "100")
        private long totalElements;
        @ApiModelProperty(value = "Total number of pages.", required = true, example = "5")
        private int totalPages;

        public Pagination() {
        }

        public Pagination(int number, int numberOfElements, int size, long totalElements, int totalPages) {
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
            return "PageMetadata [number=" + number + ", numberOfElements=" + numberOfElements + ", size=" + size + ", totalElements="
                    + totalElements + ", totalPages=" + totalPages + "]";
        }
    }

    @Override
    public String toString() {
        return "PageResultDTO [content=" + content + ", pageMetadata=" + pagination + ", getContent()=" + getContent() + ", getPageMetadata()="
                + getPagination() + "]";
    }

}