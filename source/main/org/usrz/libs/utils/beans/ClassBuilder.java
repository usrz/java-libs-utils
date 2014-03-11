/* ========================================================================== *
 * Copyright 2014 USRZ.com and Pier Paolo Fumagalli                           *
 * -------------------------------------------------------------------------- *
 * Licensed under the Apache License, Version 2.0 (the "License");            *
 * you may not use this file except in compliance with the License.           *
 * You may obtain a copy of the License at                                    *
 *                                                                            *
 *  http://www.apache.org/licenses/LICENSE-2.0                                *
 *                                                                            *
 * Unless required by applicable law or agreed to in writing, software        *
 * distributed under the License is distributed on an "AS IS" BASIS,          *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
 * See the License for the specific language governing permissions and        *
 * limitations under the License.                                             *
 * ========================================================================== */
package org.usrz.libs.utils.beans;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;

/**
 * A {@link ClassBuilder} <em>automagically</em> creates getters and setters.
 *
 * <p>Pier says: <quote>"I'm tired of writing the same stupid boiler-place
 * code, all the time... It's F***ING BORING".</quote></p>
 *
 * <p>Getters <b>must</b> conform to the naming convention <code>getFoo()</code>
 * or <code>isFoo()</code> and (obviously) they can <b>not</b> have
 * any parameter.</p>
 *
 * <p>Setters follow the same rules: names must be <code>setFoo(...)</code>
 * and they <b>must</b> declare <b>one and only one</b> parameter.</p>
 *
 * <p>Setters <b>may</b> return a value, and if so this <b>must</b> be of the
 * same type of the interface being implemented: this is to allow the
 * automatic creation of <em>builders</em> or something along the lines of:</p>
 *
 * <pre>
 * public interface MyBuilder {
 *     public MyBuilder setParameter(String parameter);
 * }
 * </pre>
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
public abstract class ClassBuilder {

    /* Logger, use Java for reuse */
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    /* Random for creating class names */
    private final Random random = new Random();

    /** The Javassist {@link ClassPool} to use specified at construction. */
    protected final ClassPool classPool;

    /* ====================================================================== */

    /**
     * Create a new {@link ClassBuilder} instance.
     *
     * <p>The {@link ClassPool} used by the newly created instance will be the
     * {@linkplain ClassPool#getDefault() default} one.</p>
     */
    ClassBuilder() {
        this(ClassPool.getDefault());
    }

    /**
     * Create a new {@link ClassBuilder} instance using the specified
     * {@link ClassPool}.
     */
    ClassBuilder(ClassPool classPool) {
        if (classPool == null) throw new NullPointerException("Null ClassPool");
        this.classPool = classPool;
    }

    /* ====================================================================== */

    /* Format a message, extra stuff */
    private String formatMessage(String format, Object[] arguments) {
        final Object[] converted = new Object[arguments.length];
        for (int x = 0; x < arguments.length; x ++) {
            if (arguments[x] == null) continue;
            if (arguments[x] instanceof CtMethod) {
                final CtMethod method = (CtMethod) arguments[x];
                converted[x] = method.getDeclaringClass().getName() + "." +
                               method.getName() + method.getSignature();
            } else if (arguments[x] instanceof CtField) {
                converted[x] = ((CtField) arguments[x]).getName();
            } else if (arguments[x] instanceof CtClass) {
                converted[x] = ((CtClass) arguments[x]).getName();
            } else {
                converted[x] = arguments[x];
            }
        }
        return String.format(format, converted);
    }

    /**
     * Convenience method used to append debug output.
     */
    void debug(String format, Object... arguments) {
        if (! logger.isLoggable(Level.FINE)) return;
        logger.fine(formatMessage(format, arguments));
    }

    /**
     * Convenience method used to append trace output.
     */
    void trace(String format, Object... arguments) {
        if (! logger.isLoggable(Level.FINER)) return;
        logger.finer(formatMessage(format, arguments));
    }

    /**
     * Convenience method used to create {@link IllegalStateException}s.
     */
    IllegalStateException exception(String format, Object... arguments) {
        return new IllegalStateException(formatMessage(format, arguments));
    }

    /* ====================================================================== */

    /**
     * Create the <em>setter</em> method specified, depending on whatever
     * behavior concrete implementations of this class generate.
     */
    abstract CtMethod createSetter(CtClass concreteClass, CtMethod method, String fieldName)
    throws NotFoundException, CannotCompileException;

    /**
     * Create the <em>getter</em> method specified, depending on whatever
     * behavior concrete implementations of this class generate.
     */
    abstract CtMethod createGetter(CtClass concreteClass, CtMethod method, String fieldName)
    throws NotFoundException, CannotCompileException;

    /* ====================================================================== */

    /**
     * Convert a getter or setter method name to a field name.
     */
    String fieldName(CtMethod method, int length) {
        final String methodName = method.getName();
        if (methodName.length() > length) {
            return Character.toLowerCase(methodName.charAt(length)) + methodName.substring(length + 1);
        }
        throw exception("Unable to extract field name for method %s", method);
    }

    /**
     * Create a concrete implementation of the specified <em>method</em>.
     *
     * <p>This default implementation only handles two kinds of methods:
     * getters (like <code>getValue()</code> or <code>isValue()</code>) or
     * setters (like <code>setValue(&hellip;)</code>) and will delegate creation
     * to either the {@link #createGetter(CtClass, CtMethod, String)} method
     * or {@link #createSetter(CtClass, CtMethod, String)}.</p>
     *
     * <p>This method can be overridden to allow more method types.</p>
     */
    CtMethod createMethod(CtClass concreteClass, CtMethod method)
    throws NotFoundException, CannotCompileException {

        final String methodName = method.getName();
        final CtClass[] parameters = method.getParameterTypes();

        if (methodName.startsWith("set") && (parameters.length == 1))
            return createSetter(concreteClass, method, fieldName(method, 3));

        if (methodName.startsWith("get") && (parameters.length == 0))
            return createGetter(concreteClass, method, fieldName(method, 3));

        if (methodName.startsWith("is") && (parameters.length == 0))
            return createGetter(concreteClass, method, fieldName(method, 2));

        throw exception("Unable to implement method %s", method);

    }

    /**
     * Create a {@link CtClass} from the specified name and super-class.
     *
     * <p>This method can be overridden to allow customization of created
     * classes before getters and setters are created.</p>
     */
    CtClass createClass(String className, CtClass superClass)
    throws NotFoundException, CannotCompileException {
        return classPool.makeClass(className, superClass);
    }

    /**
     * Create a {@link CtClass} given an <em>abstract class</em> and a
     * (possibly empty) list of <em>interfaces</em>, then implement all those
     * methods left abstract calling {@link #createMethod(CtClass, CtMethod)}.
     */
    final CtClass createClass(Class<?> abstractClass, Class<?>[] interfaces)
    throws NotFoundException, CannotCompileException {

        /* Normalize the interfaces */
        final Set<Class<?>> interfacesSet = new HashSet<>();
        for (Class<?> interfaceClass: interfaces)
            interfacesSet.add(interfaceClass);

        /* Start creating our base class (name is random) */
        final String className = abstractClass.getName() + "_" + Integer.toHexString(random.nextInt());
        final CtClass concreteClass;
        if (abstractClass.isInterface()) {

            /* If the class to implement is an interface, just extend Object */
            final CtClass objectClass = classPool.getCtClass(Object.class.getName());
            concreteClass = createClass(className, objectClass);

            /* Remember to add the missing methods */
            interfacesSet.add(abstractClass);
        } else {

            /* If the class to implement is an (?) abstract, extend it */
            final CtClass superClass = classPool.getCtClass(abstractClass.getName());
            concreteClass = createClass(className, superClass);

            /* Instrument all abstract methods from the super class */
            for (CtMethod method: superClass.getMethods()) {
                if ((method.getModifiers() & Modifier.ABSTRACT) == 0) continue;
                createMethod(concreteClass, method);
            }

            /* This is a nightmare: bridge classes for generics! Basically,
             * if we have a generic interface:
             *
             * public interface MyInterface<T> { public T getFoo(); }
             *
             * and we are extending it, but re-declaring the generic method
             * abstract:
             *
             * public abstract class MyClass implements MyInterface<Foo> {
             *   public abstract Foo getFoo();
             * }
             *
             * somehow there are two methods created:
             *   getFoo()Ljava/lang/Object
             * and
             *   getFoo()Lmy/package/Foo
             *
             * The first will be implemented, simply calling the second, but
             * the second one will be left hanging in "abstract" mode. We need
             * to get it out of the CtClass by hand (as get[Declared]Methods()
             * does not return it) and implement it too.
             */
            for (CtMethod method: superClass.getDeclaredMethods()) {
                if ((method.getModifiers() & Modifier.ABSTRACT) == 0) continue;

                /* Let's see what we can figure out */
                boolean shouldAddMethod;
                try {

                    /* Try to "get" the method that was declared abstract by the superclass */
                    final CtMethod bridgeMethod = concreteClass.getMethod(method.getName(), method.getSignature());

                    /* Is it in the current class and *currently* still abstract??? */
                    shouldAddMethod = (bridgeMethod.getModifiers() & Modifier.ABSTRACT) != 0;

                } catch (NotFoundException exception) {

                    /* The method was not found, implement it */
                    shouldAddMethod = true;
                }

                /* Either the method was not found, or it was abstract. In both cases, add it */
                if (shouldAddMethod) createMethod(concreteClass, method);

            }
        }

        /* Note what we've done */
        debug("Created class %s extending %s", concreteClass, concreteClass.getSuperclass());

        /* Constructor and superclasses are done, on to interfaces */
        for (Class<?> currentInterface: interfacesSet) {

            /* No multiple inheritance, please! */
            if (!currentInterface.isInterface()) {
                throw exception("Class %s is not an interface", currentInterface);
            }

            /* Get the interface and add it to our concrete class */
            final CtClass interfaceClass = classPool.getCtClass(currentInterface.getName());
            concreteClass.addInterface(interfaceClass);
            debug("Adding interface class %s to class %s", interfaceClass, concreteClass);

            /* Let's see what methods we have to implement */
            for (CtMethod method: interfaceClass.getMethods()) {

                /* It "should" be abstract, but still */
                if ((method.getModifiers() & Modifier.ABSTRACT) == 0) continue;

                /* Same technique as bridge methods above */
                boolean shouldAddMethod;
                try {
                    final CtMethod interfaceMethod = concreteClass.getMethod(method.getName(), method.getSignature());
                    shouldAddMethod = (interfaceMethod.getModifiers() & Modifier.ABSTRACT) != 0;
                } catch (NotFoundException exception) {
                    shouldAddMethod = true;
                }

                /* Instrument if needed */
                if (shouldAddMethod) createMethod(concreteClass, method);
            }
        }

        /* All done, return our class */
        return concreteClass;
    }

    /* ====================================================================== */

    /* Convenience */
    private static final Class<?>[] EMPTY_INTERFACES = { };

    /**
     * Create a concrete version of the specified
     * {@linkplain Class abstract class} (or interface).
     */
    public final <T> Class<T> newClass(Class<?> abstractClass) {
        return this.newClass(abstractClass, EMPTY_INTERFACES);
    }

    /**
     * Create a concrete version of the specified
     * {@linkplain Class abstract class} (or interface), also implementing
     * all the specified extra interfaces.
     */
    @SuppressWarnings("unchecked")
    public final <T> Class<T> newClass(Class<?> abstractClass, Class<?>... interfaces) {
        if (abstractClass == null) throw new NullPointerException("Null class to implement");
        if (abstractClass.isPrimitive()) throw new IllegalArgumentException(abstractClass.getName() + " is a primitive");
        if (interfaces == null) interfaces = EMPTY_INTERFACES;
        try {
            return classPool.toClass(createClass(abstractClass, interfaces));
        } catch (NotFoundException | CannotCompileException exception) {
            throw new IllegalStateException("Unable to create implementation of " + abstractClass.getName(), exception);
        }
    }

}
