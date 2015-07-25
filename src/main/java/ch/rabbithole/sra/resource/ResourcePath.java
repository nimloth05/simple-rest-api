package ch.rabbithole.sra.resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.core.PathSegment;

import ch.rabbithole.sra.impl.PathSegmentImpl;

/**
 *
 */
public final class ResourcePath implements Iterable<PathSegment> {

  //the order is important
  private List<PathSegment> segments;

  private ResourcePath(final List<PathSegment> segments) {
    this.segments = segments;
  }

  public static ResourcePath parse(String pathInfo) {

    if (pathInfo.startsWith("/")) {
      pathInfo = pathInfo.substring(1);
    }

    String[] parts = pathInfo.split("/");
    if (parts.length == 0) {
      throw new IllegalArgumentException("Empty path");
    }

    if (parts.length == 1 && parts[0].isEmpty()) {
      return new ResourcePath(Collections.<PathSegment>emptyList());
    }

    ArrayList<PathSegment> result = new ArrayList<>(parts.length);
    for (String part : parts) {
      result.add(new PathSegmentImpl(part));
    }
    return new ResourcePath(result);

//    ResourcePath lastPath = null;
//    for (int i = parts.length - 1; i > -1; --i) {
//      if (parts[i].isEmpty()) {
//        continue;
//      }
//      lastPath = new ResourcePath(parts[i], lastPath);
//    }
//
//    return lastPath;
  }

  public ResourcePath addSubPath(final ResourcePath subPath) {
    final ArrayList<PathSegment> pathSegments = new ArrayList<>(segments.size() + subPath.getPathSegments().size());
    pathSegments.addAll(segments);
    pathSegments.addAll(subPath.getPathSegments());
    return new ResourcePath(pathSegments);
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    Iterator<PathSegment> iterator = segments.iterator();
    while (iterator.hasNext()) {
      PathSegment next = iterator.next();
      builder.append(next.getPath());
      if (iterator.hasNext()) {
        builder.append("/");
      }
    }
    return builder.toString();
  }

  public List<PathSegment> getPathSegments() {
    return segments;
  }

  @Override
  public Iterator<PathSegment> iterator() {
    return segments.iterator();
  }

  public ResourcePath disjoint(ResourcePath resourcePath) {
    ArrayList<PathSegment> newList = new ArrayList<>(segments);
    newList.removeAll(resourcePath.getPathSegments());
    return new ResourcePath(newList);
  }

  public static ResourcePath empty() {
    return new ResourcePath(Collections.<PathSegment>emptyList());
  }
}
