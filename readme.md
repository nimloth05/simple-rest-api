Very simple JSR 305 implementation. This implementation can be used in conjunction with javascript frameworks such as AngularJs:

- It can only handle text/plain and application/json (you can provide additional mime types by implementing your own MessageBodyWriter/MessageBodyReader
- Use of setStatus() over sendError(). Usually, sendError() sends the error page via a html page (this can be configured or changed depending on the servlet container) but this
is not so useful if you communicate via JSON with your client.

This implementation uses Guice for dependency injection.
As a JSON converter GSON is used. This object cannot be configured at the moment.

Example usage:
Derive from

```java
  @Singleton
  public static class MyDispatcherServlet extends DispatchServlet {

    @Inject
    public MyDispatcherServlet(final Injector injector) {
      super(injector);
    }

    @Override
    protected ResourceConfiguration getConfiguration() {
      ResourceConfiguration configuration = new ResourceConfiguration();
      return configuration.addAll(getClasses());
    }

    public Set<Class<?>> getClasses() {
      return Sets.<Class<?>>newHashSet(
          ExampleRestApiClass.class
      );
    }
  }
```

You have to extensions points in this class:
- createObjectFactory()
- getMessageBodyWriterProvider()

The ObjectFactory is responsible for instantiating resource objects. The default implementation uses Guice.
The MessageBodyProvider is responsible for providing MessageBodyWriter/Reader.

Since you derive directly from the Servlet, you can actually re-implement how the framework handles various HTTP Requests.
