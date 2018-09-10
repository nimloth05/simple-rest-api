package ch.rabbithole.sra;

import ch.rabbithole.sra.resource.ObjectFactory;
import ch.rabbithole.sra.resource.ResponseWriter;
import ch.rabbithole.sra.resource.message.MessageBodyReaderWriterProvider;
import ch.rabbithole.sra.resource.message.MessageBodyReaderWriterRegistry;
import com.google.inject.Injector;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("WeakerAccess")
@Singleton
public abstract class DispatchServlet extends HttpServlet {

  private final ObjectFactory objectFactory;
  private final ResourceConfiguration configuration;
  private final MessageBodyReaderWriterProvider readerWriterRegistry;

  private static final Logger log = Logger.getLogger(ResponseWriter.class.getName());

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
  protected void doDelete(final HttpServletRequest req, final HttpServletResponse resp) {
    try {
      configuration.executeResource(readerWriterRegistry, objectFactory, HttpVerb.DELETE, req, resp);
    } catch (Exception e) {
      handleException(e, req, resp);
    }
  }

  @Override
  protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) {
    try {
      configuration.executeResource(readerWriterRegistry, objectFactory, HttpVerb.GET, req, resp);
    } catch (Exception e) {
      handleException(e, req, resp);
    }
  }

  @Override
  protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) {
    try {
      configuration.executeResource(readerWriterRegistry, objectFactory, HttpVerb.POST, req, resp);
    } catch (Exception e) {
      handleException(e, req, resp);
    }
  }

  @Override
  protected void doPut(final HttpServletRequest req, final HttpServletResponse resp) {
    try {
      configuration.executeResource(readerWriterRegistry, objectFactory, HttpVerb.PUT, req, resp);
    } catch (Exception e) {
      handleException(e, req, resp);
    }
  }

  protected void handleException(final Throwable error, HttpServletRequest req, HttpServletResponse resp) {
    log.log(Level.SEVERE, "Error during resource execution", error);
    final Response response = ResponseUtil.fromError(error);
    new ResponseWriter(getMessageBodyWriterProvider()).writeResponse(resp, response, null, new Annotation[0]);
  }

  protected abstract ResourceConfiguration getConfiguration();

  protected MessageBodyReaderWriterProvider getMessageBodyWriterProvider() {
    return MessageBodyReaderWriterRegistry.createWithDefaults();
  }
}
