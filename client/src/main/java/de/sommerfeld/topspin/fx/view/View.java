package de.sommerfeld.topspin.fx.view;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a view which must be named respectively to the FXML file.
 *
 * <p>A View is not a Frame, but could also be a component etc.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface View {
}
