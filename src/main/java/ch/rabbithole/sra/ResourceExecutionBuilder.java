package ch.rabbithole.sra;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MultivaluedMap;

import ch.rabbithole.sra.impl.UriInfoImpl;
import ch.rabbithole.sra.resource.ObjectFactory;
import ch.rabbithole.sra.resource.Resource;
import ch.rabbithole.sra.resource.ResourceExecution;
import ch.rabbithole.sra.resource.message.MessageBodyReaderWriterProvider;

/**
 * TODO JavaDoc
 */
public final class ResourceExecutionBuilder {

  private Resource resource;
  private MultivaluedMap<String, String> pathParams;

  public Resource getResource() {
    return this.resource;
  }

  public ResourceExecutionBuilder setResource(Resource resource) {
    this.resource = resource;
    return this;
  }

  public MultivaluedMap<String, String> getPathParams() {
    return this.pathParams;
  }

  public ResourceExecutionBuilder setPathParams(MultivaluedMap<String, String> pathParams) {
    this.pathParams = pathParams;
    return this;
  }

  public ResourceExecution build(final MessageBodyReaderWriterProvider readerWriterRegistry,
                                 final ObjectFactory factory,
                                 final HttpServletRequest request,
                                 final HttpServletResponse response) {
    final String domainPart = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();

    UriInfoImpl uriInfo = UriInfoImpl.create(domainPart,
                                             request.getRequestURI(),
                                             request.getPathInfo(),
                                             request.getQueryString(),
                                             pathParams);
    return new ResourceExecution(readerWriterRegistry, factory, resource, uriInfo, request, response);
  }

  public String getMethodName() {
    return resource.getMethodName();
  }
}
