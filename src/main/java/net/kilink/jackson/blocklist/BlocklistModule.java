package net.kilink.jackson.blocklist;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.impl.FailingSerializer;
import com.google.common.reflect.ClassPath;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class BlocklistModule extends SimpleModule {

    private BlocklistModule(Map<Class<?>, FailingSerializer> serializers) {
        super("blocklist");
        serializers.forEach(this::addSerializer);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private final Set<String> packages = new HashSet<>();
        private final Set<Class<? extends Annotation>> annotations = new HashSet<>();
        private final Set<Class<?>> classes = new HashSet<>();
        private final ClassPath classPath = getClassPath();

        @SafeVarargs
        public final Builder annotations(Class<? extends Annotation> annotation, Class<? extends Annotation> ...annotations) {
            this.annotations.add(Objects.requireNonNull(annotation));
            Collections.addAll(this.annotations, annotations);
            return this;
        }

        public Builder annotations(Collection<Class<? extends Annotation>> annotations) {
            this.annotations.addAll(Objects.requireNonNull(annotations));
            return this;
        }

        public Builder classes(Class<?> klazz, Class<?> ...classes) {
            this.classes.add(Objects.requireNonNull(klazz));
            Collections.addAll(this.classes, classes);
            return this;
        }

        public Builder classes(Collection<Class<?>> classes) {
            this.classes.addAll(Objects.requireNonNull(classes));
            return this;
        }

        public Builder packages(String packageName, String ...packageNames) {
            this.packages.add(Objects.requireNonNull(packageName));
            Collections.addAll(this.packages, packageNames);
            return this;
        }

        public Builder packages(Collection<String> packageNames) {
            packages.addAll(Objects.requireNonNull(packageNames));
            return this;
        }

        public BlocklistModule build() {
            Map<Class<?>, FailingSerializer> serializers = new HashMap<>();
            for (Class<?> klazz : classes) {
                serializers.put(klazz, failingDeserializer(klazz));
            }

            Set<Class<?>> packageClasses = packages.stream()
                    .flatMap(packageName -> classPath.getTopLevelClassesRecursive(packageName).stream())
                    .map(ClassPath.ClassInfo::load)
                    .collect(Collectors.toSet());

            for (Class<?> klazz : packageClasses) {
                if (!serializers.containsKey(klazz)) {
                    serializers.put(klazz, failingDeserializer(klazz));
                }
            }

            if (!annotations.isEmpty()) {
                Set<Class<?>> annotatedClasses = classPath.getAllClasses().stream()
                        .flatMap(this::safeLoad)
                        .filter(clazz -> annotations.stream().anyMatch(clazz::isAnnotationPresent))
                        .collect(Collectors.toSet());

                for (Class<?> klazz : annotatedClasses) {
                    if (!serializers.containsKey(klazz)) {
                        serializers.put(klazz, failingDeserializer(klazz));
                    }
                }
            }

            return new BlocklistModule(serializers);
        }

        private Stream<Class<?>> safeLoad(ClassPath.ClassInfo classInfo) {
            try {
                return Stream.of(classInfo.load());
            } catch (Throwable t) {
                return Stream.empty();
            }
        }


        private ClassPath getClassPath() {
            try {
                return ClassPath.from(this.getClass().getClassLoader());
            } catch (IOException ioe) {
                throw new UncheckedIOException(ioe);
            }
        }

        private FailingSerializer failingDeserializer(Class<?> klazz) {
            return new FailingSerializer(String.format("Attempted to serialize disallowed class %s", klazz.getSimpleName()));
        }
    }
}
