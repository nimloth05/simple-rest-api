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
    ResourceNode node = getNode(path, verb, tree, map);
    return new ResourceExecution((Resource) node, factory, map);
  }

  private ResourceNode getNode(final ResourcePath path, final HttpVerb verb, final ResourceTree tree, final ParameterMap map) {
    if (path == null) {
      if (tree.getResource(verb) != null) {
        return tree.getResource(verb);
      }
      return null;
    }

    ResourceTree subTree = tree.getSubTree(path.getPart());
    if (subTree != null) {
      return getNode(path.getSubPath(), verb, subTree, map);
    }

    for (Map.Entry<String, ResourceNode> treeEntry : tree.getEntries()) {
      final String key = treeEntry.getKey();
      if (key.startsWith("{") && key.endsWith("}")) {
        ResourceNode subNode = getNode(path.getSubPath(), verb, (ResourceTree) treeEntry.getValue(), map);

        if (subNode != null) {
          final String paramId = key.substring(1, key.length() - 1);
          map.addParameter(paramId, path.getPart());
          return subNode;
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
    ResourcePath subPath = resourcePath;
    while (subPath != null) {
      tree = tree.getOrCreateTree(subPath.getPart());
      subPath = subPath.getSubPath();
    }

    return tree;
  }

}
