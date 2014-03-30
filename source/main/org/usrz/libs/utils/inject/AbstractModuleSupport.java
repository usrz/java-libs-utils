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
package org.usrz.libs.utils.inject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.function.Function;

import org.aopalliance.intercept.MethodInterceptor;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.MembersInjector;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Scope;
import com.google.inject.Stage;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.binder.AnnotatedConstantBindingBuilder;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.matcher.Matcher;
import com.google.inject.spi.Message;
import com.google.inject.spi.TypeConverter;
import com.google.inject.spi.TypeListener;

public abstract class AbstractModuleSupport<B extends Binder> extends ModuleSupport<B> {

    protected AbstractModuleSupport(Function<Binder, B> conversion) {
        super((binder) ->
            conversion.apply(Objects.requireNonNull(binder, "Null binder")
                                    .skipSources(AbstractModuleSupport.class)));
    }

    protected void addError(Message message) {
        binder().addError(message);
    }

    protected void addError(String message, Object... arguments) {
        binder().addError(message, arguments);
    }

    protected void addError(Throwable t) {
        binder().addError(t);
    }

    protected <T> LinkedBindingBuilder<T> bind(Key<T> key) {
        return binder().bind(key);
    }

    protected <T> AnnotatedBindingBuilder<T> bind(TypeLiteral<T> typeLiteral) {
        return binder().bind(typeLiteral);
    }

    protected <T> AnnotatedBindingBuilder<T> bind(Class<T> type) {
        return binder().bind(type);
    }

    protected AnnotatedConstantBindingBuilder bindConstant() {
        return binder().bindConstant();
    }

    protected void bindInterceptor(Matcher<? super Class<?>> classMatcher, Matcher<? super Method> methodMatcher, MethodInterceptor... interceptors) {
        binder().bindInterceptor(classMatcher, methodMatcher, interceptors);
    }

    protected void bindListener(Matcher<? super TypeLiteral<?>> typeMatcher, TypeListener listener) {
        binder().bindListener(typeMatcher, listener);
    }

    protected void bindScope(Class<? extends Annotation> annotationType, Scope scope) {
        binder().bindScope(annotationType, scope);
    }

    protected void convertToTypes(Matcher<? super TypeLiteral<?>> typeMatcher, TypeConverter converter) {
        binder().convertToTypes(typeMatcher, converter);
    }

    protected Stage currentStage() {
        return binder().currentStage();
    }

    protected <T> MembersInjector<T> getMembersInjector(TypeLiteral<T> typeLiteral) {
        return binder().getMembersInjector(typeLiteral);
    }

    protected <T> MembersInjector<T> getMembersInjector(Class<T> type) {
        return binder().getMembersInjector(type);
    }

    protected <T> Provider<T> getProvider(Key<T> key) {
        return binder().getProvider(key);
    }

    protected <T> Provider<T> getProvider(Class<T> type) {
        return binder().getProvider(type);
    }

    protected void install(Module module) {
        binder().install(module);
    }

    protected void requestInjection(Object instance) {
        binder().requestInjection(instance);
    }

    protected void requestStaticInjection(Class<?>... types) {
        binder().requestStaticInjection(types);
    }

    protected void requireBinding(Key<?> key) {
        binder().getProvider(key);
    }

    protected void requireBinding(Class<?> type) {
        binder().getProvider(type);
    }

}
