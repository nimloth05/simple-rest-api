package ch.rabbithole.sra;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.rabbithole.sra.resource.ObjectFactory;
import ch.rabbithole.sra.resource.ResourceExecution;
import ch.rabbithole.sra.resource.ResourcePath;
import ch.rabbithole.sra.resource.message.MessageBodyReaderWriterProvider;

/**
 * Class containing the configuration of all the REST Api resources
 */
public final class ResourceConfiguration {

  private final ResourceManager resources = new ResourceManager();

  public final ResourceConfiguration addClass(final Class<?> clazz) {
    resources.addResource(clazz);
    return this;
  }

  public final ResourceConfiguration addAll(final Set<Class<?>> classes) {
    for (Class<?> aClass : classes) {
      addClass(aClass);
    }
    return this;
  }

  public void executeResource(final MessageBodyReaderWriterProvider readerWriterRegistry,
                              final ObjectFactory factory,
                              final HttpVerb verb,
                              final HttpServletRequest req,
                              final HttpServletResponse resp) {
    ResourcePath path = ResourcePath.parse(req.getPathInfo());
    ResourceExecutionBuilder builder = resources.getResource(path, verb);

    ResourceExecution resourceExecution = builder.build(readerWriterRegistry, factory, req, resp);
    try {
      resourceExecution.execute();
    } catch (Exception e) {
      throw new RuntimeException("Error during resource execution: " + path, e);
    }
  }
}
