package ch.rabbithole.sra.resource;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import ch.rabbithole.sra.HttpVerb;

/**
 * TODO JavaDoc
 */
public final class ResourceTree implements ResourceNode {

  private Map<String, ResourceNode> treeMap = new HashMap<>();

  public ResourceTree getOrCreateTree(final String part) {
    ResourceTree tree = (ResourceTree) treeMap.get(part);
    if (tree == null) {
      tree = new ResourceTree();
      treeMap.put(part, tree);
    }
    return tree;
  }

  public ResourceTree getSubTree(final String part) {
    return (ResourceTree) treeMap.get(part);
  }

  public void addResource(final HttpVerb verb, final Resource resource) {
    treeMap.put(verb.name(), resource);
  }

  public Resource getResource(final HttpVerb verb) {
    return (Resource) treeMap.get(verb.name());
  }

  public Collection<Map.Entry<String, ResourceNode>> getEntries() {
    return Collections.unmodifiableCollection(treeMap.entrySet());
  }
}
