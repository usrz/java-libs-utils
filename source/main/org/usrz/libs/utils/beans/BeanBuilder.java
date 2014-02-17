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
import javassist.bytecode.analysis.Type;

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
    public BeanBuilder(ClassPool classPool) {
        super(classPool);
    }

    /* ====================================================================== */

    /**
     * Create a setter method and (if not available) a related field to store
     * the value into.
     */
    @Override
    protected CtMethod createSetter(CtClass concreteClass, CtMethod method, String fieldName)
    throws NotFoundException, CannotCompileException {

        trace("Instrumenting setter %s", method);

        final CtClass parameterType = method.getParameterTypes()[0];

        try {
            final CtField oldField = concreteClass.getField(fieldName);
            if (!oldField.getType().equals(parameterType))
                throw exception("Field \"%s\" types mismatch: expected %s but found %s",
                                fieldName, parameterType, oldField.getType());

            trace("Skipping existing field \"%s\" declaration instrumenting setter %s", fieldName, method.getName());
        } catch (NotFoundException exception) {
            final CtField newField = new CtField(parameterType, fieldName, concreteClass);
            concreteClass.addField(newField);
            trace("Adding field \"%s\" of type %s instrumenting setter %s", fieldName, parameterType, method.getName());
        }

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
                                             .append(" value) { this.")
                                             .append(fieldName)
                                             .append(" = value; ");
        if (returnThis) body.append("return this; ");
        body.append('}');

        trace("Generated: %s ", body);

        final CtMethod setter = CtMethod.make(body.toString(), concreteClass);
        concreteClass.addMethod(setter);
        return setter;
    }

    /**
     * Create a setter gethod and (if not available) a related field to gather
     * the value from.
     */
    @Override
    protected CtMethod createGetter(CtClass concreteClass, CtMethod method, String fieldName)
    throws NotFoundException, CannotCompileException {

        trace("Instrumenting getter %s", method);

        final CtClass returnType = method.getReturnType();
        try {
            final CtField oldField = concreteClass.getField(fieldName);
            if (!oldField.getType().equals(returnType))
                throw exception("Field \"%s\" types mismatch: expected %s but found %s",
                                fieldName, returnType, oldField.getType());
            trace("Skipping existing field \"%s\" declaration instrumenting getter %s", fieldName, method.getName());
        } catch (NotFoundException exception) {
            final CtField newField = new CtField(returnType, fieldName, concreteClass);
            newField.setModifiers(Modifier.PUBLIC);
            concreteClass.addField(newField);
            trace("Adding field \"%s\" of type %s instrumenting getter %s", fieldName, returnType, method.getName());
        }

        final String body = new StringBuilder(Modifier.toString(method.getModifiers() ^ Modifier.ABSTRACT))
                                      .append(' ')
                                      .append(returnType.getName())
                                      .append(' ')
                                      .append(method.getName())
                                      .append("() { return this.")
                                      .append(fieldName)
                                      .append("; }")
                                      .toString();

        trace("Generated: %s ", body);

        CtMethod getter = CtMethod.make(body, concreteClass);
        concreteClass.addMethod(getter);
        return getter;

    }
}
