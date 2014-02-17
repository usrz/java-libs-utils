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

public class MapperBuilder extends ClassBuilder {

    /* The BeanBuilder we'll use to create the "mappedProperties()" getter */
    private final BeanBuilder beanBuilder;

    public MapperBuilder() {
        super();
        beanBuilder = new BeanBuilder(classPool);
    }

    public MapperBuilder(ClassPool classPool) {
        super(classPool);
        beanBuilder = new BeanBuilder(this.classPool);
    }

    /* ====================================================================== */

    @Override
    protected CtClass createClass(String className, CtClass superClass)
    throws NotFoundException, CannotCompileException {
        final CtClass concreteClass = super.createClass(className, superClass);

        final CtClass mapperClass = classPool.get(Mapper.class.getName());

        debug("Adding interface %s to class %s", mapperClass, concreteClass);
        concreteClass.addInterface(mapperClass);

        final CtMethod method = mapperClass.getMethod("mappedProperties", "()Ljava/util/Map;");
        beanBuilder.createGetter(concreteClass, method, "_mappedProperties");

        final CtField uninitialized = concreteClass.getField("_mappedProperties");
        final CtField initialized = CtField.make("private final java.util.Map _mappedProperties = new java.util.HashMap();", concreteClass);
        concreteClass.removeField(uninitialized);
        concreteClass.addField(initialized);

        System.err.println("MEMBER IS -> " + initialized);

        return concreteClass;
    }

    @Override
    protected CtMethod createSetter(CtClass concreteClass, CtMethod method, String fieldName)
    throws NotFoundException, CannotCompileException {

        trace("Instrumenting setter %s", method);

        final CtClass parameterType = method.getParameterTypes()[0];

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
                                             .append(" value) { this._mappedProperties.put(\"")
                                             .append(fieldName)
                                             .append("\", ");

        if (parameterType.isPrimitive()) {
            switch (parameterType.getName()) {
                case "boolean": body.append("new java.lang.Boolean(");   break;
                case "byte":    body.append("new java.lang.Byte(");      break;
                case "char":    body.append("new java.lang.Character("); break;
                case "short":   body.append("new java.lang.Short(");     break;
                case "int":     body.append("new java.lang.Integer(");   break;
                case "long":    body.append("new java.lang.Long(");      break;
                case "float":   body.append("new java.lang.Float(");     break;
                case "double":  body.append("new java.lang.Double(");    break;
                default: throw exception("Unsupported primitive %s for getter %s", returnType, method);
            }
            body.append("value)");
        } else {
            body.append("value");
        }

        body.append("); ");
        if (returnThis) body.append("return this; ");
        body.append('}');

        trace("Generated: %s ", body);

        final CtMethod setter = CtMethod.make(body.toString(), concreteClass);
        concreteClass.addMethod(setter);
        return setter;

    }

    @Override
    protected CtMethod createGetter(CtClass concreteClass, CtMethod method, String fieldName)
    throws NotFoundException, CannotCompileException {

        trace("Instrumenting getter %s", method);

        final CtClass returnType = method.getReturnType();
        final StringBuilder body = new StringBuilder(Modifier.toString(method.getModifiers() ^ Modifier.ABSTRACT))
                                             .append(' ')
                                             .append(returnType.getName())
                                             .append(' ')
                                             .append(method.getName())
                                             .append("() { return ");

        if (returnType.isPrimitive()) switch (returnType.getName()) {
            case "boolean": body.append("((java.lang.Boolean) ");   break;
            case "byte":    body.append("((java.lang.Byte) ");      break;
            case "char":    body.append("((java.lang.Character) "); break;
            case "short":   body.append("((java.lang.Short) ");     break;
            case "int":     body.append("((java.lang.Integer) ");   break;
            case "long":    body.append("((java.lang.Long) ");      break;
            case "float":   body.append("((java.lang.Float) ");     break;
            case "double":  body.append("((java.lang.Double) ");    break;
            default: throw exception("Unsupported primitive %s for getter %s", returnType, method);
        } else {
            body.append("(").append(returnType.getName()).append(')');
        }

        body.append("this._mappedProperties.get(\"")
            .append(fieldName)
            .append("\")");

        if (returnType.isPrimitive()) switch (returnType.getName()) {
            case "boolean": body.append(").booleanValue()"); break;
            case "byte":    body.append(").byteValue()");    break;
            case "char":    body.append(").charValue()");    break;
            case "short":   body.append(").shortValue()");   break;
            case "int":     body.append(").intValue()");     break;
            case "long":    body.append(").longValue()");    break;
            case "float":   body.append(").floatValue()");   break;
            case "double":  body.append(").doubleValue()");  break;
            default: throw exception("Unsupported primitive %s for getter %s", returnType, method);
        }

        body.append("; }");

        trace("Generated: %s ", body);

        CtMethod getter = CtMethod.make(body.toString(), concreteClass);
        concreteClass.addMethod(getter);
        return getter;

    }

    /* ====================================================================== */

}
