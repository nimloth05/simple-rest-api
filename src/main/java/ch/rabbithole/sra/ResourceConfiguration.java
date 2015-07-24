package ch.rabbithole.sra;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.rabbithole.sra.resource.ObjectFactory;
import ch.rabbithole.sra.resource.ResourceExecution;
import ch.rabbithole.sra.resource.ResourcePath;

/**
 * Class containing the configuration of all the REST Api resources
 */
public final class ResourceConfiguration {

  private final ResourceManager resources = new ResourceManager();

  public final ResourceConfiguration addClass(final Class<?> clazz) {
    resources.addResource(clazz);
    return this;
  }

  public final ResourceConfiguration addAll(final Set<Class<?>> classes)  {
    for (Class<?> aClass : classes) {
      addClass(aClass);
    }
    return this;
  }

  public void executeResource(final ObjectFactory factory, final HttpVerb verb, final HttpServletRequest req, final HttpServletResponse resp) {
    ResourcePath path = ResourcePath.parse(req.getPathInfo());
    ResourceExecution resourceExecution = resources.getResource(path, verb, factory);
    resourceExecution.execute(req, resp);
  }
}
