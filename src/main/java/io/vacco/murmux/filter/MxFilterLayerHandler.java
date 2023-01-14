package io.vacco.murmux.filter;

import com.sun.net.httpserver.HttpExchange;
import io.vacco.murmux.http.MxHandler;
import io.vacco.murmux.http.MxRequest;
import io.vacco.murmux.http.MxResponse;

import java.util.function.Consumer;

/**
 * Handler for multiple FilterLayer.
 */
public class MxFilterLayerHandler {

  @SuppressWarnings("rawtypes")
  private final MxFilterLayer[] layers;

  public MxFilterLayerHandler(int layers) {
    this.layers = new MxFilterLayer<?>[layers];
    for (int i = 0; i < this.layers.length; i++) {
      this.layers[i] = new MxFilterLayer<>();
    }
  }

  public MxResponse handle(HttpExchange httpExchange) {
    MxRequest request = new MxRequest(httpExchange);
    MxResponse response = new MxResponse(httpExchange);
    for (MxFilterLayer<?> chain : layers) {
      chain.filter(request, response);
      if (response.isClosed()) {
        break;
      }
    }
    return response;
  }

  /**
   * Add a new handler for a specific handler layers.
   *
   * @param level   The layers.
   * @param handler The handler, will be appended to the top of the layers.
   */
  @SuppressWarnings("unchecked")
  public void add(int level, MxHandler handler) {
    if (level >= layers.length) {
      throw new IndexOutOfBoundsException("Out of bounds: " + level + " > " + layers.length);
    }
    if (level < 0) {
      throw new IndexOutOfBoundsException("Cannot be under zero: " + level + " < 0");
    }
    layers[level].add(handler);
  }

  /**
   * Merge two FilterLayerHandler
   *
   * @param filterLayerHandler The FilterLayerHandler which you want to merge with this
   */
  @SuppressWarnings("unchecked")
  public void combine(MxFilterLayerHandler filterLayerHandler) {
    if (filterLayerHandler != null) {
      MxFilterLayer<?>[] chains = filterLayerHandler.getLayers();
      if (chains.length != layers.length) {
        throw new IllegalArgumentException(
          "Cannot add a filterLayerHandler with different layers sizes: "
            + chains.length
            + " != "
            + layers.length);
      }
      for (int i = 0; i < chains.length; i++) {
        layers[i].addAll(chains[i].getFilter());
      }
    }
  }

  /**
   * Iterate over the different FilterLayer
   *
   * @param layerConsumer A consumer for the layers
   */
  public void forEach(Consumer<MxFilterLayer<?>> layerConsumer) {
    if (layerConsumer == null) {
      return;
    }
    for (MxFilterLayer<?> layer : layers) {
      layerConsumer.accept(layer);
    }
  }

  private MxFilterLayer<?>[] getLayers() {
    return layers;
  }
}
