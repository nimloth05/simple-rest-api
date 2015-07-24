package ch.rabbithole.sra;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

import javax.ws.rs.Path;

import ch.rabbithole.sra.resource.ObjectFactory;
import ch.rabbithole.sra.resource.ParameterMap;
import ch.rabbithole.sra.resource.Resource;
import ch.rabbithole.sra.resource.ResourceExecution;
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

  public ResourceExecution getResource(ResourcePath path, HttpVerb verb, final ObjectFactory factory) {
    ResourceTree tree = rootTree;

    ParameterMap map = new ParameterMap();

    while (path != null) {
      ResourceTree newTree = tree.getSubTree(path.getPart());
      if (newTree == null) {
        newTree = getTreeForParameter(tree, map, path, verb);
      }
      tree = newTree;
      path = path.getSubPath();
    }

    Resource resource = tree.getResource(verb);
    return new ResourceExecution(resource, factory, map);
  }

  private String getSubPath(final Method resourceMethod) {
    Path annotation = resourceMethod.getAnnotation(Path.class);
    return annotation != null ? annotation.value() : "";
  }

  private ResourceTree getTreeForParameter(final ResourceTree tree, final ParameterMap map, final ResourcePath path, final HttpVerb verb) {
    for (Map.Entry<String, ResourceNode> entrySet : tree.getEntries()) {
      if (entrySet.getKey().startsWith("{") && entrySet.getKey().endsWith("}")) {

        ResourceNode node = entrySet.getValue();
        if (!(node instanceof ResourceTree)) {
          throw new IllegalStateException("Resource path does not end in a HTTP verb: " + entrySet.getKey());
        }
        ResourceTree subTree = (ResourceTree) node;
        if (!subTree.contains(verb)) {
          continue;
        }

        final String key = entrySet.getKey().substring(1, entrySet.getKey().length() - 1);
        map.addParameter(key, path.getPart());

        return (ResourceTree) entrySet.getValue();
      }
    }
    throw new IllegalArgumentException("No path found");
  }

  private ResourceTree addPath(final ResourcePath resourcePath) {
    ResourceTree tree = rootTree;
    ResourcePath subPath = resourcePath;
    while (subPath != null) {
      tree = tree.getOrCreateTree(subPath.getPart());
      subPath = subPath.getSubPath();
    }

    return tree;
  }

}
