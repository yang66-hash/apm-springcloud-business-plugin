package com.yang.apm.springplugin;

import co.elastic.apm.agent.sdk.ElasticApmInstrumentation;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.simple.CountingMode;
import io.micrometer.core.instrument.simple.SimpleConfig;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

import static net.bytebuddy.matcher.ElementMatchers.*;

public class ICESSpringBusinessInstrumentation extends ElasticApmInstrumentation {
    @Override
    public ElementMatcher<? super TypeDescription> getTypeMatcher() {

        ElementMatcher.Junction<? super TypeDescription> annotatedWithService = isAnnotatedWith(Service.class).and(not(isInterface())).and(not(isAbstract()));
        ElementMatcher.Junction<? super TypeDescription> annotatedWithCom = isAnnotatedWith(Component.class)
                .and(nameMatches("(?i).*?(service|impl).*?"))
                .and(not(isInterface()))
                .and(not(isAbstract()));
        // 包装匹配器，输出匹配的类名
        return hasSuperType(annotatedWithService.or(annotatedWithCom));
    }

    @Override
    public ElementMatcher<? super MethodDescription> getMethodMatcher() {
        return ElementMatchers.isMethod().and(ElementMatchers.isPublic());
    }

    @Override
    public Collection<String> getInstrumentationGroupNames() {
        return Collections.singletonList("my-spring-cloud-plugin");
    }

    @Override
    public String getAdviceClassName(){
        return "com.yang.apm.springplugin.ICESSpringBusinessInstrumentation$AdviceClass";
    }
    /**
     * This advice class is applied when the instrumentation identifies
     * it needs to be applied, ie when the above matchers ({@code getTypeMatcher}
     * and {@code getMethodMatcher}) have been matched
     *
     * The ELastic APM Java agent provides a metrics capability using
     * the Micrometer framework, see
     * https://www.elastic.co/guide/en/apm/agent/java/current/metrics.html#metrics-micrometer
     */
    public static class AdviceClass {
        /**
         * At method entry, we want to ensure that we've registered the
         * with the micrometer registry (only needed to do once, so
         * it's guarded with a boolean), then we'll increment
         * a page count metric, `page_count` which will be available
         * in the Elastic APM metrics views.
         *
         * For details on the Byte Buddy advice annotation used here,
         * see the ExampleHttpServerInstrumentation$AdviceClass
         * class and it's `onEnterHandle` method javadoc, in this package.
         */
        private static ConcurrentHashMap<String, Counter> methodCounters = new ConcurrentHashMap<>();
        private static volatile boolean metricWasAdded = false;
        @Advice.OnMethodEnter(suppress = Throwable.class, inline = false)
        public static void onEnterHandle(@Advice.Origin String method) {
            if (!metricWasAdded) {
                SimpleMeterRegistry registry = new SimpleMeterRegistry(new SimpleConfig() {
                    @Override
                    public CountingMode mode() {
                        // to report the delta since the last report
                        // this makes building dashboards a bit easier
                        return CountingMode.STEP;
                    }
                    @Override
                    public Duration step() {
                        // the duration should match metrics_interval, which defaults to 30s
                        return Duration.ofSeconds(30);
                    }

                    @Override
                    public String get(String key) {
                        return null;
                    }
                }, Clock.SYSTEM);
                Metrics.globalRegistry.add(registry);
                metricWasAdded = true;
            }
            String methodName = method;
            System.out.println(methodName);
            methodCounters.computeIfAbsent(methodName, name->{
               return Metrics.counter("method_call_counter", "method",name);
            });
            Metrics.counter("method_call_counter", "method",methodName).increment(1);
        }
    }
}
