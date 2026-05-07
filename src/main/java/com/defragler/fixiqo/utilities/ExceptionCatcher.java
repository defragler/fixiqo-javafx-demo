package com.defragler.fixiqo.utilities;

public final class ExceptionCatcher {
    
    private ExceptionCatcher() { }

    public static String resolveCallerClass() {
        return StackWalker.getInstance()
              .walk(stream -> stream.skip(2).findFirst())
              .map(frame -> {
                  String full = frame.getClassName();
                  return full.substring(full.lastIndexOf('.') + 1);
              })
              .orElse("Unknown");
    }

    public static String resolveCallerChain() {
        return StackWalker.getInstance()
              .walk(stream -> stream
                    .skip(2)
                    .limit(5)
                    .map(f -> f.getClassName().substring(f.getClassName().lastIndexOf('.') + 1))
                    .reduce((a, b) -> a + " -> " + b)
                    .orElse("Unknown"));
    }
}
