package ru.tbank.edu.ab.questions;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Proxy;


public class ProxyTest {

    @Test
    void jdkDynamicProxyTest() {
        final var service = new BusinessServiceImpl();
        var proxy = (BusinessService) Proxy.newProxyInstance(
                ProxyTest.class.getClassLoader(),
                new Class[] { BusinessService.class },
                (__, method, args) -> {
                    if (method.isAnnotationPresent(Proxied.class)) {
                        System.out.println("[JdkDynamicProxy]");
                    }
                    return method.invoke(service, args);
                });

        proxy.businessMethod1();
    }

    @Test
    void cglibProxyTest() {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(BusinessServiceImpl.class);
        enhancer.setInterfaces(new Class[] { BusinessService.class });
        enhancer.setCallback((MethodInterceptor) (obj, method, args, proxy) -> {
            if (method.isAnnotationPresent(Proxied.class)) {
                System.out.println("[CglibProxy]");
            }
            return proxy.invokeSuper(obj, args);
        });

        var proxy = (BusinessServiceImpl) enhancer.create();
        proxy.businessMethod1();
        proxy.businessMethod2();
    }

    public interface BusinessService {

        @Proxied
        void businessMethod1();

    }

    public static class BusinessServiceImpl implements BusinessService {

        @Proxied
        @Override
        public void businessMethod1() {
            System.out.println("BusinessServiceImpl#businessMethod1");
            businessMethod2();
        }

        @Proxied
        public void businessMethod2() {
            System.out.println("BusinessServiceImpl#businessMethod2");
        }

    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Proxied {

    }

}
