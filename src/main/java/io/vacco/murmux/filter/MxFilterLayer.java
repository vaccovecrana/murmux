package io.vacco.murmux.filter;

import io.vacco.murmux.http.MxHandler;
import io.vacco.murmux.http.MxRequest;
import io.vacco.murmux.http.MxResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

/**
 * Controller class for Filter Layer.
 */
public class MxFilterLayer<T extends MxHandler> {

  private final List<T> filter = Collections.synchronizedList(new ArrayList<>());

  public void add(T filter) {
    this.filter.add(filter);
  }

  public void add(int index, T filter) {
    this.filter.add(index, filter);
  }

  public void addAll(List<T> filter) {
    this.filter.addAll(filter);
  }

  public List<T> getFilter() {
    return filter;
  }

  void filter(MxRequest req, MxResponse res) {
    ListIterator<T> iter = this.filter.listIterator();
    while (!res.isClosed() && iter.hasNext()) {
      iter.next().handle(req, res);
    }
  }
}
