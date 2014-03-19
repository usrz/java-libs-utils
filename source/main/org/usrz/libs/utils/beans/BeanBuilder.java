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

import com.google.inject.Inject;

/**
 * A {@link BeanBuilder} is a {@link ClassBuilder} creating getters and setters
 * methods backed by (private) fields..
 *
 * <p>Getter <b>must</b> conform to the naming convention <code>getFoo()</code>
 * or <code>isFoo()</code>: their return type <b>must</b> be the precisely the
 * same type of the field <code>foo</code> (if this is declared already in an
 * abstract class). If the field is not declared or inherited, one (private)
 * will be automatically created.</p>
 *
 * <p>Setters follow the same rules: names must be <code>setFoo(...)</code>
 * and they <b>must</b> declare <b>one and only one</b> parameter, of precisely
 * the same type of the field <code>foo</code> (same rules that apply to
 * getters apply here).</p>
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
public class BeanBuilder extends ClassBuilder {

    /**
     * Create a new {@link BeanBuilder}.
     *
     * @see ClassBuilder#ClassBuilder()
     */
    public BeanBuilder() {
        super();
    }

    /**
     * Create a new {@link BeanBuilder} using the specifed {@link ClassPool}.
     *
     * @see ClassBuilder#ClassBuilder(ClassPool)
     */
    @Inject
    public BeanBuilder(ClassPool classPool) {
        super(classPool);
    }

    /* ====================================================================== */

    CtField createField(CtClass concreteClass, CtMethod method, String fieldName, CtClass fieldType)
    throws CannotCompileException {
        try {
            final CtField oldField = concreteClass.getField(fieldName);
            if (!oldField.getType().equals(fieldType))
                throw exception("Field \"%s\" types mismatch: expected %s but found %s",
                                fieldName, fieldType, oldField.getType());

            log.trace("Skipping existing field \"%s\" declaration instrumenting method %s", fieldName, method.getName());
            return oldField;

        } catch (NotFoundException exception) {
            final CtField newField = new CtField(fieldType, fieldName, concreteClass);

            /* Add a "@JsonIgnore" attribute (helps Jackson) */
            final ClassFile cf = concreteClass.getClassFile();
            final ConstPool cp = cf.getConstPool();
            final AnnotationsAttribute attr = new AnnotationsAttribute(cp, AnnotationsAttribute.visibleTag);
            attr.setAnnotation(new Annotation("com.fasterxml.jackson.annotation.JsonIgnore", cp));
            newField.getFieldInfo().addAttribute(attr);

            /* Add the field and proceed */
            concreteClass.addField(newField);
            log.trace("Adding field \"%s\" of type %s instrumenting method %s", fieldName, fieldType, method.getName());
            return newField;
        }
    }

    /**
     * Create a setter method and (if not available) a related field to store
     * the value into.
     */
    @Override
    CtMethod createSetter(CtClass concreteClass, CtMethod method, String fieldName)
    throws NotFoundException, CannotCompileException {

        log.trace("Instrumenting setter %s", method);

        fieldName = "__" + fieldName + "__";
        final CtClass parameterType = method.getParameterTypes()[0];
        createField(concreteClass, method, fieldName, parameterType);

        final Type returnType = Type.get(method.getReturnType());
        final Type concreteType = Type.get(concreteClass);
        boolean returnThis;
        if (returnType.equals(Type.VOID)) {
            returnThis = false;
        } else if (returnType.isAssignableFrom(concreteType)) {
            returnThis = true;
        } else {
            throw exception("Unable to implement method %s", method);
        }

        final StringBuilder body = new StringBuilder(Modifier.toString(method.getModifiers() ^ Modifier.ABSTRACT))
                                             .append(' ')
                                             .append(method.getReturnType().getName())
                                             .append(' ')
                                             .append(method.getName())
                                             .append('(')
                                             .append(parameterType.getName())
                                             .append(" value) { ");

        /* Nullability checks */
        if (method.hasAnnotation(NotNullable.class)) {
            if (parameterType.isPrimitive()) {
                throw exception("Unable to check for nullablility of primitives in " + method);
            } else {
                body.append(" if (value == null) { throw new IllegalArgumentException(\"Invalid null value for setter \\\"")
                    .append(method.getName())
                    .append('(')
                    .append(parameterType.getSimpleName())
                    .append(")\\\"\"); } ");
            }
        }

        /* Bean values protection */
        if (method.hasAnnotation(Protected.class)) {
            if (parameterType.isPrimitive()) {
                throw exception("Unable to protect primitive setter " + method);
            } else {
                body.append(" if (this.")
                    .append(fieldName)
                    .append(" != null) { throw new IllegalStateException(\"Protected setter \\\"")
                    .append(method.getName())
                    .append('(')
                    .append(parameterType.getSimpleName())
                    .append(")\\\" already invoked\"); } ");
            }
        }

        body.append("this.").append(fieldName).append(" = value; ");
        if (returnThis) body.append("return this; ");
        body.append('}');

        log.trace("Generated: %s ", body);

        final CtMethod setter = CtMethod.make(body.toString(), concreteClass);
        concreteClass.addMethod(setter);
        return setter;
    }

    /**
     * Create a setter gethod and (if not available) a related field to gather
     * the value from.
     */
    @Override
    CtMethod createGetter(CtClass concreteClass, CtMethod method, String fieldName)
    throws NotFoundException, CannotCompileException {

        log.trace("Instrumenting getter %s", method);

        fieldName = "__" + fieldName + "__";
        final CtClass returnType = method.getReturnType();
        createField(concreteClass, method, fieldName, returnType);

        final String body = new StringBuilder(Modifier.toString(method.getModifiers() ^ Modifier.ABSTRACT))
                                      .append(' ')
                                      .append(returnType.getName())
                                      .append(' ')
                                      .append(method.getName())
                                      .append("() { return this.")
                                      .append(fieldName)
                                      .append("; }")
                                      .toString();

        log.trace("Generated: %s ", body);

        CtMethod getter = CtMethod.make(body, concreteClass);
        concreteClass.addMethod(getter);
        return getter;

    }
}
