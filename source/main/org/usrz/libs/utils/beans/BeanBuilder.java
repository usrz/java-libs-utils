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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.analysis.Type;
import javassist.bytecode.annotation.Annotation;

/**
 * A {@link BeanBuilder} <em>automagically</em> creates bean getters, setters
 * and (private) fields to any class.
 *
 * <p>Pier says: <quote>"I'm tired of writing the same stupid boiler-place
 * code, all the time... It's F***ING BORING".</quote></p>
 *
 * <p>Getters <b>must</b> conform to the naming convention <code>getFoo()</code>
 * or <code>isFoo()</code>: their return type <b>must</b> be the precisely the
 * same type of the field <code>foo</code> (if this is declared already in an
 * abstract class). If the field is not declared or inherited, one (private)
 * will be automatically created. Obviously, getters can <b>not</b> have
 * any parameter.</p>
 *
 * <p>Setters follow the same rules: names must be <code>setFoo(...)</code>
 * and they <b>must</b> declare <b>one and only one</b> parameter, of precisely
 * the same type of the field <code>foo</code> (same rules that apply to
 * getters apply here).</p>
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
 * <p>Note that because of the <b>strict</b> requirements of getter, setter and
 * field types, it is therefore <b>impossible</b> to have constructs like:</p>
 *
 * <pre>
 * public interface MyInterface {
 *     public Number getNumber();
 *     public void setNumber(Integer number);
 * }
 * </pre>
 *
 * <p>even if obviously <code>Number</code> is assignable directly from
 * <code>Integer</code>.</p>
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
public class BeanBuilder {

    private static final Logger logger = Logger.getLogger(BeanBuilder.class.getName());
    private static final Class<?>[] EMPTY_INTERFACES = { };
    private final ClassPool classPool = ClassPool.getDefault();
    private final Random random = new Random();

    private String formatMethod(CtMethod method) {
        return method.getDeclaringClass().getName() + "." +
               method.getName() + method.getSignature();
    }

    private String fieldName(CtMethod method, int length) {
        final String methodName = method.getName();
        if (methodName.length() > length) {
            return Character.toLowerCase(methodName.charAt(length)) + methodName.substring(length + 1);
        }
        throw new IllegalStateException("Unable to extract field name for method " + formatMethod(method));
    }

    private CtMethod instrument(CtClass concreteClass, CtMethod method)
    throws NotFoundException, CannotCompileException {

        final String methodName = method.getName();
        final CtClass[] parameters = method.getParameterTypes();

        if (methodName.startsWith("set") && (parameters.length == 1))
            return instrumentSetter(concreteClass, method, fieldName(method, 3));

        if (methodName.startsWith("get") && (parameters.length == 0))
            return instrumentGetter(concreteClass, method, fieldName(method, 3));

        if (methodName.startsWith("is") && (parameters.length == 0))
            return instrumentGetter(concreteClass, method, fieldName(method, 2));

        throw new IllegalStateException("Unable to implement method " + formatMethod(method));

    }

    private CtMethod instrumentSetter(CtClass concreteClass, CtMethod method, String fieldName)
    throws NotFoundException, CannotCompileException {

        if (logger.isLoggable(Level.FINER)) logger.finer("Instrumenting setter " + formatMethod(method));

        final CtClass parameterType = method.getParameterTypes()[0];

        try {
            final CtField oldField = concreteClass.getField(fieldName);
            if (!oldField.getType().equals(parameterType))
                throw new IllegalStateException("Field \"" + fieldName + "\" types mismatch. Expected " +
                                                method.getReturnType().getName() + " but found " +
                                                oldField.getType().getName());
            if (logger.isLoggable(Level.FINER)) logger.finer("Skipping existing field declaration " + fieldName + " in setter");
        } catch (NotFoundException exception) {
            final CtField newField = new CtField(parameterType, fieldName, concreteClass);
            concreteClass.addField(newField);
            if (logger.isLoggable(Level.FINER)) logger.finer("Adding field " + parameterType.getName() + " " + fieldName + " for setter");
        }

        final Type returnType = Type.get(method.getReturnType());
        final Type concreteType = Type.get(concreteClass);
        boolean returnThis;
        if (returnType.equals(Type.VOID)) {
            returnThis = false;
        } else if (returnType.isAssignableFrom(concreteType)) {
            returnThis = true;
        } else {
            throw new IllegalStateException("Unable to implement method " + formatMethod(method));
        }

        final StringBuilder body = new StringBuilder(Modifier.toString(method.getModifiers() ^ Modifier.ABSTRACT))
                                             .append(' ')
                                             .append(method.getReturnType().getName())
                                             .append(' ')
                                             .append(method.getName())
                                             .append('(')
                                             .append(parameterType.getName())
                                             .append(" value) { this.")
                                             .append(fieldName)
                                             .append(" = value; ");
        if (returnThis) body.append("return this; ");
        body.append('}');

        if (logger.isLoggable(Level.FINER)) logger.finer("Body for setter " + formatMethod(method) + ": " + body.toString());

        final CtMethod setter = CtMethod.make(body.toString(), concreteClass);
        concreteClass.addMethod(setter);
        return setter;
    }

    private CtMethod instrumentGetter(CtClass concreteClass, CtMethod method, String fieldName)
    throws NotFoundException, CannotCompileException {

        if (logger.isLoggable(Level.FINER)) logger.finer("Instrumenting setter " + formatMethod(method));

        try {
            final CtField oldField = concreteClass.getField(fieldName);
            if (!oldField.getType().equals(method.getReturnType()))
                throw new IllegalStateException("Field \"" + fieldName + "\" types mismatch. Expected " +
                                                method.getReturnType().getName() + " but found " +
                                                oldField.getType().getName());
            if (logger.isLoggable(Level.FINER)) logger.finer("Skipping existing field declaration " + fieldName + " in getter");
        } catch (NotFoundException exception) {
            final CtField newField = new CtField(method.getReturnType(), fieldName, concreteClass);
            newField.setModifiers(Modifier.PUBLIC);
            concreteClass.addField(newField);
            if (logger.isLoggable(Level.FINER)) logger.finer("Adding field " + method.getReturnType().getName() + " " + fieldName + " for getter");
        }

        final String body = new StringBuilder(Modifier.toString(method.getModifiers() ^ Modifier.ABSTRACT))
                                      .append(' ')
                                      .append(method.getReturnType().getName())
                                      .append(' ')
                                      .append(method.getName())
                                      .append("() { return this.")
                                      .append(fieldName)
                                      .append("; }")
                                      .toString();

        if (logger.isLoggable(Level.FINER)) logger.finer("Body for getter " + formatMethod(method) + ": " + body);

        CtMethod getter = CtMethod.make(body, concreteClass);
        concreteClass.addMethod(getter);
        return getter;

    }

    private Class<?> createClass(Class<?> fieldAnnotation, Class<?> abstractClass, Class<?>[] interfaces)
    throws NotFoundException, CannotCompileException {

        /* Primitive? Forget it! */
        if (abstractClass.isPrimitive())
            throw new IllegalArgumentException(abstractClass.getName() + " is a primitive");

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
            concreteClass = classPool.makeClass(className, objectClass);

            /* Remember to add the missing methods */
            interfacesSet.add(abstractClass);
        } else {

            /* If the class to implement is an (?) abstract, extend it */
            final CtClass superClass = classPool.getCtClass(abstractClass.getName());
            concreteClass = classPool.makeClass(className, superClass);

            /* Instrument all abstract methods from the super class */
            for (CtMethod method: superClass.getMethods()) {
                if ((method.getModifiers() & Modifier.ABSTRACT) == 0) continue;
                instrument(concreteClass, method);
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
                if (shouldAddMethod) instrument(concreteClass, method);

            }
        }

        if (logger.isLoggable(Level.FINE)) logger.fine("Created class " + concreteClass.getName() + " extending " + concreteClass.getSuperclass().getName());

        /* Constructor and superclasses are done, on to interfaces */
        for (Class<?> currentInterface: interfacesSet) {

            /* No multiple inheritance, please! */
            if (!currentInterface.isInterface()) {
                throw new IllegalArgumentException(currentInterface.getName() + " is not an interface");
            }

            /* Get the interface and add it to our concrete class */
            final CtClass interfaceClass = classPool.getCtClass(currentInterface.getName());
            concreteClass.addInterface(interfaceClass);
            if (logger.isLoggable(Level.FINE)) logger.fine("Adding interface class " + interfaceClass.getName() + " to class " + concreteClass.getName());

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
                if (shouldAddMethod) instrument(concreteClass, method);
            }
        }

        /* Annotate fields? */
        if (fieldAnnotation != null) {
            final String annotationName = fieldAnnotation.getName();
            final ClassFile concreteClassFile = concreteClass.getClassFile();
            final ConstPool constpool = concreteClassFile.getConstPool();
            for (CtField field: concreteClass.getDeclaredFields()) {
                System.err.println("Should Annotate " + field);

                Annotation annot = new Annotation(annotationName, constpool);

                AnnotationsAttribute attr = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
                attr.addAnnotation(annot);
                //annot.addMemberValue("value", new IntegerMemberValue(ccFile.getConstPool(), 0));
                field.getFieldInfo().addAttribute(attr);
    //            concreteClassFile.addAttribute(attr);

            }
        }

        /* All done, return our class */
        return classPool.toClass(concreteClass);
    }

    /* ====================================================================== */

    public <T> Class<T> newClass(Class<?> abstractClass) {
        return this.newClass(null, abstractClass, EMPTY_INTERFACES);
    }

    public <T> Class<T> newClass(Class<?> abstractClass, Class<?>... interfaces) {
        return this.newClass(null, abstractClass, interfaces);
    }

    public <T> Class<T> newClass(Class<? extends java.lang.annotation.Annotation> fieldAnnotation, Class<?> abstractClass) {
        return this.newClass(fieldAnnotation, abstractClass, EMPTY_INTERFACES);
    }

    @SuppressWarnings("unchecked")
    public <T> Class<T> newClass(Class<? extends java.lang.annotation.Annotation> fieldAnnotation, Class<?> abstractClass, Class<?>... interfaces) {
        if (abstractClass == null) throw new NullPointerException("Null class to implement");
        if (interfaces == null) interfaces = EMPTY_INTERFACES;
        try {
            return (Class<T>) createClass(fieldAnnotation, abstractClass, interfaces);
        } catch (NotFoundException | CannotCompileException exception) {
            throw new IllegalStateException("Unable to create implementation of " + abstractClass.getName(), exception);
        }
    }

    /* ====================================================================== */

    public static <T> T newInstance(Class<?> concreteClass) {
        return newInstance(concreteClass, (Object[]) null);
    }

    @SuppressWarnings("unchecked")
    public static <T> T newInstance(Class<?> concreteClass, Object... parameters) {

        final Class<?>[] classes = new Class<?>[parameters == null ? 0 : parameters.length];
        if (parameters == null) parameters = new Object[0];
        for (int x = 0; x < parameters.length; x++) {
            if (parameters[x] != null) classes[x] = parameters[x].getClass();
        }

        try {
            final Constructor<?> constructor = concreteClass.getConstructor(classes);
            if (logger.isLoggable(Level.FINE))
                logger.fine("Creating new instance of " + concreteClass.getName() + " with " + constructor);
            constructor.setAccessible(true);
            return (T) constructor.newInstance(parameters);

        } catch (NoSuchMethodException exception) {
            // No constructor found, upwards and onwards!
        } catch (InvocationTargetException exception) {
            final Throwable cause = exception.getCause();
            if (cause instanceof RuntimeException) throw (RuntimeException) cause;
            if (cause instanceof Error) throw (Error) cause;
            throw new IllegalStateException("Constructor of " + concreteClass.getName() + " threw an exception", exception);
        } catch (InstantiationException  | IllegalAccessException exception) {
            throw new IllegalStateException("Unable to create instance of " + concreteClass.getName(), exception);
        }

        try {
            for (Constructor<?> constructor: concreteClass.getDeclaredConstructors()) {
                if (isAssignable(constructor.getParameterTypes(), parameters)) {
                    if (logger.isLoggable(Level.FINE))
                        logger.fine("Creating new instance of " + concreteClass.getName() + " with " + constructor);
                    constructor.setAccessible(true);
                    return (T) constructor.newInstance(parameters);
                }
            }

            throw new IllegalStateException("No constructor for " + concreteClass.getName() + " found with specified parameters");

        } catch (InvocationTargetException exception) {
            final Throwable cause = exception.getCause();
            if (cause instanceof RuntimeException) throw (RuntimeException) cause;
            if (cause instanceof Error) throw (Error) cause;
            throw new IllegalStateException("Constructor of " + concreteClass.getName() + " threw an exception", exception);
        } catch (InstantiationException | IllegalAccessException exception) {
            throw new IllegalStateException("Unable to create instance of " + concreteClass.getName(), exception);
        }
    }

    private static boolean isAssignable(Class<?>[] types, Object[] parameters) {
        if (types.length != parameters.length) return false;
        for (int x = 0; x < types.length; x ++) {
            if (parameters[x] == null) continue;
            if (types[x].isInstance(parameters[x])) continue;
            return false;
        }
        return true;
    }

}
