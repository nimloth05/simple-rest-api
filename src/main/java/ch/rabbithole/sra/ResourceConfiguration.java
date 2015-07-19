package ch.rabbithole.sra;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Class containing the configuration of all the REST Api resources
 */
public final class ResourceConfiguration {

  private final ResourceManager resources = new ResourceManager();

  public final void addClass(final Class<?> clazz) {
    //analyze Root-Annotation
    //analyze public method, extract all path annotation
    //Insert into resource tree
    //all parameter {} parts in a resources will be replaced with a * in the resource tree
    //we can have several nodes with the same path for different http verbs
  }

  public void executeResource(final HttpVerb verb, final HttpServletRequest req, final HttpServletResponse resp) {
    ResourcePath path = ResourcePath.parse(req.getPathInfo());
    ResourceExecution resourceExecution = resources.getResource(path, verb);
    resourceExecution.execute(req, resp);
  }
}
