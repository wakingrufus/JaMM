module jamm.desktop {
    requires jaudiotagger;
    requires jamm.common;
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.media;
    requires kotlin.stdlib;
    requires kotlin.reflect;
    requires org.slf4j;
    requires java.desktop;
    requires kotlinx.coroutines.core.jvm;
    requires kotlinx.coroutines.javafx;
    requires java.logging;
    requires java.prefs;
    requires org.jfxtras.styles.jmetro;

    exports com.github.wakingrufus.jamm.desktop;
    exports com.github.wakingrufus.javafx;
}