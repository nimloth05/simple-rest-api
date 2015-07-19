package ch.rabbithole.sra;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class DispatchServlet extends HttpServlet {

  @Override
  protected void doDelete(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
    super.doDelete(req, resp);
    if (resp.isCommitted()) {
      return;
    }

    ResourceConfiguration configuration = getConfiguration();
    configuration.executeResource(HttpVerb.DELETE, req, resp);
  }

  @Override
  protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
    super.doGet(req, resp);
    if (resp.isCommitted()) {
      return;
    }

    ResourceConfiguration configuration = getConfiguration();
    configuration.executeResource(HttpVerb.GET, req, resp);
  }

  @Override
  protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
    super.doPost(req, resp);
    if (resp.isCommitted()) {
      return;
    }

    ResourceConfiguration configuration = getConfiguration();
    configuration.executeResource(HttpVerb.POST, req, resp);
  }

  @Override
  protected void doPut(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
    super.doPut(req, resp);
    if (resp.isCommitted()) {
      return;
    }

    ResourceConfiguration configuration = getConfiguration();
    configuration.executeResource(HttpVerb.PUT, req, resp);
  }

  protected abstract ResourceConfiguration getConfiguration();
}
