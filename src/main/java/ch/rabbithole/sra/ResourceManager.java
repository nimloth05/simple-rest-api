package ch.rabbithole.sra;

import com.sun.jersey.api.NotFoundException;
import com.sun.ws.rs.ext.MultiValueMapImpl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Path;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;

import ch.rabbithole.sra.resource.Resource;
import ch.rabbithole.sra.resource.ResourceNode;
import ch.rabbithole.sra.resource.ResourcePath;
import ch.rabbithole.sra.resource.ResourceTree;

/**
 * Manages all the available resources in a tree like structure.
 */
public final class ResourceManager {

  private final ResourceTree rootTree = new ResourceTree();

  public void addResource(final Class<?> clazz) {
    Path annotation = clazz.getAnnotation(Path.class);
    String path = annotation.value();
    ResourcePath resourcePath = ResourcePath.parse(path);

    addResourceMethods(resourcePath, clazz.getDeclaredMethods());
  }

  private void addResourceMethods(final ResourcePath rootPath, final Method[] methods) {
    for (Method resourceMethod : methods) {
      if (Modifier.isPublic(resourceMethod.getModifiers()) && !Modifier.isStatic(resourceMethod.getModifiers())) {
        HttpVerb verb = HttpVerb.getVerb(resourceMethod);
        final String subPath = getSubPath(resourceMethod);
        ResourcePath resourcePath = rootPath.addSubPath(ResourcePath.parse(subPath));
        Resource resource = new Resource(resourceMethod);
        ResourceTree tree = addPath(resourcePath);
        tree.addResource(verb, resource);
      }
    }
  }

  @NotNull
  public ResourceExecutionBuilder getResource(ResourcePath path, HttpVerb verb) {
    ResourceTree tree = rootTree;
    MultivaluedMap<String, String> pathParams = new MultiValueMapImpl<>();
    Resource resource = getNode(path.getPathSegments(), 0, verb, tree, pathParams);

    if (resource == null) {
      throw new NotFoundException("Resource not found for path: " + path + "/" + verb);
    }

    return new ResourceExecutionBuilder()
        .setPathParams(pathParams)
        .setResource(resource);
  }

  @Nullable
  private Resource getNode(final List<PathSegment> path, final int index, final HttpVerb verb, final ResourceTree tree, final MultivaluedMap<String, String> pathParams) {
    //We exhausted all possibilities on this sub tree
    if (index == path.size()) {
      //it may match
      //or it may not and the caller has to try another sub tree.
      return tree.getResource(verb);
    }

    PathSegment pathSegment = path.get(index);

    ResourceTree subTree = tree.getSubTree(pathSegment.getPath());
    if (subTree != null) {
      return getNode(path, index+1, verb, subTree, pathParams);
    }

    for (Map.Entry<String, ResourceNode> treeEntry : tree.getEntries()) {
      final String key = treeEntry.getKey();
      if (key.startsWith("{") && key.endsWith("}")) {
        Resource resource = getNode(path, index+1, verb, (ResourceTree) treeEntry.getValue(), pathParams);

        if (resource != null) {
          final String paramId = key.substring(1, key.length() - 1);
          pathParams.putSingle(paramId, pathSegment.getPath());
          return resource;
        }
      }
    }
    return null;
  }

  private String getSubPath(final Method resourceMethod) {
    Path annotation = resourceMethod.getAnnotation(Path.class);
    return annotation != null ? annotation.value() : "";
  }

  private ResourceTree addPath(final ResourcePath resourcePath) {
    ResourceTree tree = rootTree;

    for (PathSegment pathSegment : resourcePath) {
      tree = tree.getOrCreateTree(pathSegment.getPath());
    }
    return tree;
  }

}
