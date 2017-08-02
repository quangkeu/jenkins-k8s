package vn.vnpt.ssdc.models;

import java.util.List;

public class Paginator<T> {
   public List<T> content;
   public boolean last;
   public int totalElements;
   public int totalPages;
   public int size;
   public int number;
   public String sort;
   public boolean first;
   public int numberOfElements;
}