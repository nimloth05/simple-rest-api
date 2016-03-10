package com.sun.ws.rs.ext;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MultivaluedMap;

/**
 * Implementation of the {@link MultivaluedMap} interface.
 */
public final class MultiValueMapImpl<K, V> implements MultivaluedMap<K, V> {

  private ListMultimap<K, V> backingMap = ArrayListMultimap.create();

  public MultiValueMapImpl() {

  }

  public MultiValueMapImpl(final Map<K, V> source) {
    for (Map.Entry<K, V> mapEntry : source.entrySet()) {
      putSingle(mapEntry.getKey(), mapEntry.getValue());
    }
  }

  @Override
  public void putSingle(final K key, final V value) {
    backingMap.put(key, value);
  }

  @Override
  public void add(final K key, final V value) {
    backingMap.put(key, value);
  }

  @Override
  public V getFirst(final K key) {
    List<V> vs = backingMap.get(key);
    return vs.isEmpty() ? null : vs.get(0);
  }

  @Override
  public int size() {
    return backingMap.size();
  }

  @Override
  public boolean isEmpty() {
    return backingMap.isEmpty();
  }

  @Override
  public boolean containsKey(final Object key) {
    return backingMap.containsKey(key);
  }

  @Override
  public boolean containsValue(final Object value) {
    return backingMap.containsValue(value);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<V> get(final Object key) {
    return backingMap.get((K) key);
  }

  @Override
  public List<V> put(final K key, final List<V> value) {
    return backingMap.replaceValues(key, value);
  }

  @Override
  public List<V> remove(final Object key) {
    return backingMap.removeAll(key);
  }

  @Override
  public void putAll(final Map<? extends K, ? extends List<V>> m) {
    for (Entry<? extends K, ? extends List<V>> entry : m.entrySet()) {
      backingMap.putAll(entry.getKey(), entry.getValue());
    }
  }

  @Override
  public void clear() {
    backingMap.clear();
  }

  @Override
  public Set<K> keySet() {
    return backingMap.keySet();
  }

  @Override
  public Collection<List<V>> values() {
    Collection values = backingMap.asMap().values();
    return values;
  }

  @Override
  public Set<Entry<K, List<V>>> entrySet() {
    Set entries = backingMap.asMap().entrySet();
    return entries;
  }
}
