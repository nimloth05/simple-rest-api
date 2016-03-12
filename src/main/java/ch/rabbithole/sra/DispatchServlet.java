package ch.rabbithole.sra;

import com.google.inject.Injector;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.rabbithole.sra.resource.ObjectFactory;
import ch.rabbithole.sra.resource.message.MessageBodyReaderWriterProvider;
import ch.rabbithole.sra.resource.message.MessageBodyReaderWriterRegistry;

@Singleton
public abstract class DispatchServlet extends HttpServlet {

  private final ObjectFactory objectFactory;
  private final ResourceConfiguration configuration;
  private final MessageBodyReaderWriterProvider readerWriterRegistry;

  @Inject
  public DispatchServlet(final Injector injector) {
    objectFactory = createObjectFactory(injector);
    configuration = getConfiguration();
    readerWriterRegistry = getMessageBodyWriterProvider();
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
//    super.doDelete(req, resp);
//    if (resp.isCommitted()) {
//      return;
//    }

    configuration.executeResource(readerWriterRegistry, objectFactory, HttpVerb.DELETE, req, resp);
  }

  @Override
  protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
//    super.doGet(req, resp);
//    if (resp.isCommitted()) {
//      return;
//    }

    configuration.executeResource(readerWriterRegistry, objectFactory, HttpVerb.GET, req, resp);
  }

  @Override
  protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
//    super.doPost(req, resp);
//    if (resp.isCommitted()) {
//      return;
//    }

    configuration.executeResource(readerWriterRegistry, objectFactory, HttpVerb.POST, req, resp);
  }

  @Override
  protected void doPut(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
//    super.doPut(req, resp);
//    if (resp.isCommitted()) {
//      return;
//    }

    configuration.executeResource(readerWriterRegistry, objectFactory, HttpVerb.PUT, req, resp);
  }

  protected abstract ResourceConfiguration getConfiguration();

  protected MessageBodyReaderWriterProvider getMessageBodyWriterProvider() {
    return MessageBodyReaderWriterRegistry.createWithDefaults();
  }
}
