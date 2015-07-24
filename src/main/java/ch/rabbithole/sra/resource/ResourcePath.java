package ch.rabbithole.sra.resource;

/**
 *
 */
public final class ResourcePath {

  private String part;
  private ResourcePath subPath;

  private ResourcePath(final String part, final ResourcePath subPath) {
    this.part = part;
    this.subPath = subPath;
  }

  public static ResourcePath parse(String pathInfo) {

    if (pathInfo.startsWith("/")) {
      pathInfo = pathInfo.substring(1);
    }

    String[] parts = pathInfo.split("/");
    if (parts.length == 0) {
      throw new IllegalArgumentException("Empty path");
    }

    ResourcePath lastPath = null;
    for (int i = parts.length - 1; i > -1; --i) {
      if (parts[i].isEmpty()) {
        continue;
      }
      lastPath = new ResourcePath(parts[i], lastPath);
    }

    return lastPath;
  }

  public String getPart() {
    return part;
  }

  public ResourcePath getSubPath() {
    return subPath;
  }

  public ResourcePath addSubPath(final ResourcePath subPath) {
    return append(subPath);
  }

  private ResourcePath append(final ResourcePath pathToAppend) {
    return new ResourcePath(part, this.subPath != null ? this.subPath.append(pathToAppend) : pathToAppend);
  }

  @Override
  public String toString() {
    return part + (subPath != null ? "/" + subPath : "");
  }
}
