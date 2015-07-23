package ch.rabbithole.sra;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.rabbithole.sra.resource.ResourceExecution;
import ch.rabbithole.sra.resource.ResourcePath;

/**
 * Class containing the configuration of all the REST Api resources
 */
public final class ResourceConfiguration {

  private final ResourceManager resources = new ResourceManager();

  public final void addClass(final Class<?> clazz) {
    resources.addResource(clazz);
  }

  public void executeResource(final HttpVerb verb, final HttpServletRequest req, final HttpServletResponse resp) {
    ResourcePath path = ResourcePath.parse(req.getPathInfo());
    ResourceExecution resourceExecution = resources.getResource(path, verb);
    resourceExecution.execute(req, resp);
  }
}
