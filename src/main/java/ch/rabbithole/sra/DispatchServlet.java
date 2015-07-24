package ch.rabbithole.sra;

import com.google.inject.Injector;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.rabbithole.sra.resource.ObjectFactory;

public abstract class DispatchServlet extends HttpServlet {

  private final ObjectFactory objectFactory;

  @Inject
  public DispatchServlet(final Injector injector) {
    objectFactory = createObjectFactory(injector);
  }

  /**
   * Creates the object factory which will instantiate the REST service objects.
   * Default implementation uses Guice, subclass may choose different.
   */
  protected ObjectFactory createObjectFactory(final Injector injector) {
    return injector.getInstance(GuiceObjectFactory.class);
  }

  @Override
  protected void doDelete(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
    super.doDelete(req, resp);
    if (resp.isCommitted()) {
      return;
    }

    ResourceConfiguration configuration = getConfiguration();
    configuration.executeResource(objectFactory, HttpVerb.DELETE, req, resp);
  }

  @Override
  protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
    super.doGet(req, resp);
    if (resp.isCommitted()) {
      return;
    }

    ResourceConfiguration configuration = getConfiguration();
    configuration.executeResource(objectFactory, HttpVerb.GET, req, resp);
  }

  @Override
  protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
    super.doPost(req, resp);
    if (resp.isCommitted()) {
      return;
    }

    ResourceConfiguration configuration = getConfiguration();
    configuration.executeResource(objectFactory, HttpVerb.POST, req, resp);
  }

  @Override
  protected void doPut(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
    super.doPut(req, resp);
    if (resp.isCommitted()) {
      return;
    }

    ResourceConfiguration configuration = getConfiguration();
    configuration.executeResource(objectFactory, HttpVerb.PUT, req, resp);
  }

  protected abstract ResourceConfiguration getConfiguration();
}
