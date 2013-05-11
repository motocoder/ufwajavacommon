package llc.ufwa.javacommon.test.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import junit.framework.TestCase;
import llc.ufwa.concurrency.Callback;
import llc.ufwa.concurrency.LimitingExecutorService;
import llc.ufwa.concurrency.LimitingExecutorServiceFactory;
import llc.ufwa.concurrency.ParallelControl;
import llc.ufwa.data.exception.CanceledResourceException;
import llc.ufwa.data.exception.ResourceException;
import llc.ufwa.data.resource.cache.AlwaysNullCache;
import llc.ufwa.data.resource.cache.MemoryCache;
import llc.ufwa.data.resource.loader.CachedParallelResourceLoader;
import llc.ufwa.data.resource.loader.DefaultResourceLoader;
import llc.ufwa.data.resource.loader.ParallelResourceLoader;
import llc.ufwa.data.resource.loader.ResourceEvent;
import llc.ufwa.data.resource.loader.ResourceLoader;

import org.apache.log4j.BasicConfigurator;
import org.junit.Test;

public class CachedParellelResourceLoaderTest {
    
    static {
        BasicConfigurator.configure();
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testSimpleGetExistsParellel() {
        
        final ResourceLoader<String, String> internal = new DefaultResourceLoader<String, String>() {

            @Override
            public String get(String key) throws ResourceException {
                return key;
            }
            
        };
        
        final ParallelControl<Object> control = new ParallelControl<Object>();
        
        final LimitingExecutorService limited = LimitingExecutorServiceFactory.createExecutorService(
                Executors.newFixedThreadPool(10), 
                Executors.newFixedThreadPool(100),10);
        
        final ParallelResourceLoader<String, String> parellelLoader = 
            new CachedParallelResourceLoader<String, String>(
                internal,
                limited,
                Executors.newFixedThreadPool(10),
                Executors.newFixedThreadPool(10),
                10,
                "",
                new MemoryCache<String, String>(),
                new MemoryCache<String, Boolean>()
            );
        
        {
            
            final Callback<Object, ResourceEvent<String>> callback = new Callback<Object, ResourceEvent<String>>() {
    
                @Override
                public Object call(ResourceEvent<String> value) {
                    
                    control.setValue(value);
                    control.unBlockOnce();
                    
                    return false;
                }
                
            };
                
            try {
                parellelLoader.getParallel(callback, "Hi");
            } catch (ResourceException e1) {
                TestCase.fail();
            }
            
            try {
                control.blockOnce();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
            TestCase.assertEquals("Hi", ((ResourceEvent<String>)control.getValue()).getVal());
            
        }
        
        {
            
            final Callback<Object, ResourceEvent<Boolean>> callback = new Callback<Object, ResourceEvent<Boolean>>() {
    
                @Override
                public Object call(ResourceEvent<Boolean> value) {
                    
                    control.setValue(value);
                    control.unBlockOnce();
                    
                    return false;
                }
                
            };
                
            parellelLoader.existsParallel(callback, "Hi");
            
            try {
                control.blockOnce();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
            TestCase.assertEquals(true, ((ResourceEvent<String>)control.getValue()).getVal());
            
        }
        
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void voidTestDepth() {
        
        final ParallelControl<Object> loaderControl = new ParallelControl<Object>();
        
        final ResourceLoader<String, String> internal = new DefaultResourceLoader<String, String>() {

            @Override
            public String get(String key) throws ResourceException {
                
                try {
                    loaderControl.blockOnce();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                
                return key;
            }
            
        };
        
        final ParallelControl<Object> control1 = new ParallelControl<Object>();
        final ParallelControl<Object> control2 = new ParallelControl<Object>();
        
        final LimitingExecutorService limited = LimitingExecutorServiceFactory.createExecutorService(
                Executors.newFixedThreadPool(10), 
                Executors.newFixedThreadPool(100),10);
        
        final CachedParallelResourceLoader<String, String> parellelLoader = 
            new CachedParallelResourceLoader<String, String>(
                internal,
                limited,
                Executors.newFixedThreadPool(10),
                Executors.newFixedThreadPool(10),
                1,
                "",
                new MemoryCache<String, String>(),
                new MemoryCache<String, Boolean>()
            );
        
        {
            
            final Callback<Object, ResourceEvent<String>> callback1 = new Callback<Object, ResourceEvent<String>>() {
    
                @Override
                public Object call(ResourceEvent<String> value) {
                    
                    control1.setValue(value);
                    control1.unBlockOnce();
                    
                    return false;
                }
                
            };
            
            final Callback<Object, ResourceEvent<String>> callback2 = new Callback<Object, ResourceEvent<String>>() {
                
                @Override
                public Object call(ResourceEvent<String> value) {
                    
                    control2.setValue(value);
                    control2.unBlockOnce();
                    
                    return false;
                }
                
            };
                
            parellelLoader.getParallel(callback1, "Hi");            
            parellelLoader.getParallel(callback2, "Hi");
            
            loaderControl.unBlockOnce();
            
            try {
                control1.blockOnce();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            try {
                control2.blockOnce();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
            final ResourceEvent<String> val1 = (ResourceEvent<String>) control1.getValue();
            final ResourceEvent<String> val2 = (ResourceEvent<String>) control2.getValue();
            
            TestCase.assertNull(val1.getVal());
            TestCase.assertEquals("Hi", val2.getVal());
            
            TestCase.assertTrue(val1.getThrowable() instanceof CanceledResourceException);
            
        }
        
    }
    
//    @SuppressWarnings("unchecked")
//    @Test
//    public void voidTestUnique() {
//        System.out.println("Starting testUnique");
//        
//        { //unique get
//            final ParallelControl<Object> loaderControl = new ParallelControl<Object>();
//            
//            final ParallelControl<Object> control1 = new ParallelControl<Object>();
//            final ParallelControl<Object> control2 = new ParallelControl<Object>();
//            
//            final ResourceLoader<String, String> internal = new DefaultResourceLoader<String, String>() {
//    
//                @Override
//                public String get(String key) throws ResourceException {
//                    
//                    try {                  
//                        loaderControl.blockOnce();                        
//                    } 
//                    catch (InterruptedException e) {
//                        throw new ResourceException("error");
//                    }
//                    
//                    return key;
//                }
//                
//            };
//            
//            
//            
//            final CachedParallelResourceLoader<String, String> parellelLoader = 
//                    new CachedParallelResourceLoader<String, String>(
//                            internal,
//                            new JavaCommonLimitingExecutorService(Executors.newFixedThreadPool(10),1),
//                            Executors.newFixedThreadPool(10),
//                            Executors.newFixedThreadPool(10),
//                            10,
//                            1,
//                            "",
//                            new MemoryCache<String, String>(),
//                            new MemoryCache<String, Boolean>()
//                        );
//            
//            {
//                
//                final Callback<Object, ResourceEvent<String>> callback1 = new Callback<Object, ResourceEvent<String>>() {
//        
//                    @Override
//                    public boolean call(Object source, ResourceEvent<String> value) {
//                     
//                        try {
//                            
//                            System.out.println("in 1 " + value);
//                                                      
//                                control1.setValue(value);
//                            
//                        }
//                        finally {
//                            
//                            System.out.println("out 1"); 
//                            
//                        }
//                        return false;
//                    }
//                    
//                };
//                
//                final Callback<Object, ResourceEvent<String>> callback2 = new Callback<Object, ResourceEvent<String>>() {
//                    
//                    @Override 
//                    public boolean call(Object source, ResourceEvent<String> value) {
//                        
//                        try {
//                            
//                            System.out.println("in 2 " + value);
//                                
//                            control2.setValue(value);
//                            
//                        }
//                        finally { 
//                            System.out.println("out 2");
//                        }
//                        
//                        return false;
//                    }
//                    
//                };
//                    
//                parellelLoader.getParallel(callback1, "Hi");
//                parellelLoader.getParallel(callback2, "Hi2");
//      
//                try {
//                    
//                    Thread.sleep(1000);
//                    loaderControl.unBlockAll();
//                    Thread.sleep(1000);
//                    
//                } catch (InterruptedException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
//                
//                final ResourceEvent<String> val1 = (ResourceEvent<String>) control1.getValue();
//                final ResourceEvent<String> val2 = (ResourceEvent<String>) control2.getValue();
//                
//                System.out.println("val1 " + val1);
//                System.out.println("val2 " + val2);
//                System.out.println("val1 " + val1.getVal());
//                System.out.println("val2 " + val2.getVal());
//                
//                TestCase.assertEquals("Hi2", val2.getVal());
//                TestCase.assertNull(val1.getVal());
//                
//                System.out.println(val1.getThrowable());
//                TestCase.assertTrue(val1.getThrowable() instanceof ResourceException);
//                
//            }
//            
//        }
//        
//        { //unique exists
//            final ParallelControl<Object> loaderControl = new ParallelControl<Object>();
//            
//            final ResourceLoader<String, String> internal = new DefaultResourceLoader<String, String>() {
//    
//                @Override 
//                public String get(String key) throws ResourceException {
//                    try {
//                        loaderControl.blockOnce();
//                    } 
//                    catch (InterruptedException e) {
//                        throw new ResourceException("interrupted");
//                    }
//                    return key;
//                }
//                
//            };
//            
//            final ParallelControl<Object> control1 = new ParallelControl<Object>();
//            final ParallelControl<Object> control2 = new ParallelControl<Object>();
//            
//            final ParallelResourceLoader<String, String> parellelLoader = 
//                new CachedParallelResourceLoader<String, String>(
//                    internal,
//                    new JavaCommonLimitingExecutorService(Executors.newFixedThreadPool(10),1),
//                    Executors.newFixedThreadPool(10),
//                    Executors.newFixedThreadPool(10),
//                    10,
//                    1,
//                    "",
//                    new AlwaysNullCache<String, String>(),
//                    new AlwaysNullCache<String, Boolean>()
//                );
//            
//            {
//                
//                final Callback<Object, ResourceEvent<Boolean>> callback1 = new Callback<Object, ResourceEvent<Boolean>>() {
//        
//                    @Override
//                    public boolean call(Object source, ResourceEvent<Boolean> value) {
//                        
//                        control1.setValue(value);
//                        
//                        return false;
//                    }
//                    
//                };
//                
//                final Callback<Object, ResourceEvent<Boolean>> callback2 = new Callback<Object, ResourceEvent<Boolean>>() {
//                    
//                    @Override
//                    public boolean call(Object source, ResourceEvent<Boolean> value) {
//                        
//                        control2.setValue(value);
//                        
//                        return false;
//                    }
//                    
//                };
//                    
//                parellelLoader.existsParallel(callback1, "Hi");
//                parellelLoader.existsParallel(callback2, "Hi2");
//                
//                try {
//                    
//                    Thread.sleep(1000);
//                    loaderControl.unBlockAll();
//                    Thread.sleep(1000);
//                    
//                } catch (InterruptedException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
//
//                final ResourceEvent<Boolean> val1 = (ResourceEvent<Boolean>) control1.getValue();
//                final ResourceEvent<Boolean> val2 = (ResourceEvent<Boolean>) control2.getValue();
//                
//                TestCase.assertNull(val1.getVal());
//                TestCase.assertTrue(val2.getVal());
//                
//                System.out.println(val1.getThrowable());
//                TestCase.assertTrue(val1.getThrowable() instanceof ResourceException);
//                
//            }
//            
//        }
//        
//    }
//    
    @SuppressWarnings("unchecked")
    @Test
    public void testSimpleException() {

        final ResourceLoader<String, String> internal = new DefaultResourceLoader<String, String>() {

            @Override
            public String get(String key) throws ResourceException {
                throw new ResourceException("TestException");
            }

        };

        final ParallelControl<Object> control = new ParallelControl<Object>();

        final LimitingExecutorService limited = LimitingExecutorServiceFactory.createExecutorService(
                Executors.newFixedThreadPool(10), 
                Executors.newFixedThreadPool(100),10);
        
        final ParallelResourceLoader<String, String> parellelLoader = new CachedParallelResourceLoader<String, String>(
                internal,
                limited,
                Executors.newFixedThreadPool(10),
                Executors.newFixedThreadPool(10),
                10,
                "",
                new AlwaysNullCache<String, String>(),
                new AlwaysNullCache<String, Boolean>()
            );

        {

            final Callback<Object, ResourceEvent<String>> callback = new Callback<Object, ResourceEvent<String>>() {

                @Override
                public Object call(ResourceEvent<String> value) {

                    control.setValue(value);
                    control.unBlockOnce();

                    return false;
                }

            };

            try {
                parellelLoader.getParallel(callback, "Hi");
            } catch (ResourceException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

            try {
                control.blockOnce();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            TestCase.assertNotNull(((ResourceEvent<String>) control.getValue()).getThrowable());

        }

        {

            final Callback<Object, ResourceEvent<Boolean>> callback = new Callback<Object, ResourceEvent<Boolean>>() {

                @Override
                public Object call(ResourceEvent<Boolean> value) {

                    control.setValue(value);
                    control.unBlockOnce();

                    return false;
                }

            };

            parellelLoader.existsParallel(callback, "Hi");

            try {
                control.blockOnce();
            } catch (InterruptedException e) {
                TestCase.fail();
            }

            TestCase.assertNotNull(((ResourceEvent<String>) control.getValue()).getThrowable());

        }

    }

    @Test
    public void testNegativeConstructorParameters() {

        final ResourceLoader<String, String> internal = new DefaultResourceLoader<String, String>() {

            @Override
            public String get(String key) throws ResourceException {
                throw new ResourceException("TestException");
            }

        };

        try {// null internal
            
            final LimitingExecutorService limited = LimitingExecutorServiceFactory.createExecutorService(
                    Executors.newFixedThreadPool(10), 
                    Executors.newFixedThreadPool(100),10);
            
            new CachedParallelResourceLoader<String, String>(
                null,
                limited, 
                Executors.newFixedThreadPool(10),
                Executors.newFixedThreadPool(10),
                10, 
                "",
                new MemoryCache<String, String>(),
                new MemoryCache<String, Boolean>()
                );
            TestCase.fail("Should not have gotten here");
        }
        catch(Exception e) {
            //expected
        }

        try {// null executors
            
            final LimitingExecutorService limited = LimitingExecutorServiceFactory.createExecutorService(
                    Executors.newFixedThreadPool(10), 
                    Executors.newFixedThreadPool(100),10);
            
            new CachedParallelResourceLoader<String, String>(
                internal,
                null,
                limited,
                Executors.newFixedThreadPool(10),
                10, 
                "",
                new MemoryCache<String, String>(),
                new MemoryCache<String, Boolean>()
            );
            TestCase.fail("Should not have gotten here");
            
        }
        catch(Exception e) {
            //expected
        }
        
        try { // bad depth
            
            final LimitingExecutorService limited = LimitingExecutorServiceFactory.createExecutorService(
                    Executors.newFixedThreadPool(10), 
                    Executors.newFixedThreadPool(100),10);
            
            new CachedParallelResourceLoader<String, String>(
                internal,
                limited,
                Executors.newFixedThreadPool(10), 
                Executors.newFixedThreadPool(10),
                0, 
                "",
                new MemoryCache<String, String>(),
                new MemoryCache<String, Boolean>()
            );
            TestCase.fail("Should not have gotten here");
            
        }
        catch(Exception e) {
            //expected
        }
      
        try {// null loggingTag
            
            final LimitingExecutorService limited = LimitingExecutorServiceFactory.createExecutorService(
                    Executors.newFixedThreadPool(10), 
                    Executors.newFixedThreadPool(100),10);
            
            new CachedParallelResourceLoader<String, String>(
                internal,
                limited,
                Executors.newFixedThreadPool(10),
                Executors.newFixedThreadPool(10), 
                10,
                null,
                new MemoryCache<String, String>(),
                new MemoryCache<String, Boolean>()
            );
            TestCase.fail("Should not have gotten here");
            
        }
        catch(Exception e) {
            //expected
        }
        
        try {// null cache
            
            final LimitingExecutorService limited = LimitingExecutorServiceFactory.createExecutorService(
                    Executors.newFixedThreadPool(10), 
                    Executors.newFixedThreadPool(100),10);
            
            new CachedParallelResourceLoader<String, String>(
                internal,
                limited,
                Executors.newFixedThreadPool(10),
                Executors.newFixedThreadPool(10),
                10,
                "",
                null,
                new MemoryCache<String, Boolean>()
            );
            TestCase.fail("Should not have gotten here");
            
        }
        catch(Exception e) {
            //expected
        }
        
        try {// null searchCache
            
            final LimitingExecutorService limited = LimitingExecutorServiceFactory.createExecutorService(
                    Executors.newFixedThreadPool(10), 
                    Executors.newFixedThreadPool(100),10);
            
            new CachedParallelResourceLoader<String, String>(
                internal,
                limited,
                Executors.newFixedThreadPool(10),
                Executors.newFixedThreadPool(10),
                10,
                "",
                new MemoryCache<String, String>(),
                null
            );
            TestCase.fail("Should not have gotten here");
            
        }
        catch(Exception e) {
            //expected
        }
        

    }

    @Test
    public void testNegativeParameters() {

        final ResourceLoader<String, String> internal = new DefaultResourceLoader<String, String>() {

            @Override
            public String get(String key) throws ResourceException {
                throw new ResourceException("TestException");
            }

        };

        {

            final LimitingExecutorService limited = LimitingExecutorServiceFactory.createExecutorService(
                    Executors.newFixedThreadPool(10), 
                    Executors.newFixedThreadPool(100),10);
                    
            final ParallelResourceLoader<String, String> parellel =
                    new CachedParallelResourceLoader<String, String>(
                            internal,
                            limited,
                            Executors.newFixedThreadPool(10),
                            Executors.newFixedThreadPool(10),
                            10,
                            "",
                            new MemoryCache<String, String>(),
                            new MemoryCache<String, Boolean>()
                        );

            try {

                parellel.get(null);
                TestCase.fail("Should not have gotten this far");

            } catch (Exception e) {
                // expected behavior
            }

            try {

                parellel.exists(null);
                TestCase.fail("Should not have gotten this far");

            } catch (Exception e) {
                // expected behavior
            }

            try {

                parellel.getAll(null);
                TestCase.fail("Should not have gotten this far");

            } catch (Exception e) {
                // expected behavior
            }

            try {

                final List<String> keys = new ArrayList<String>();

                keys.add(null);
                keys.add("");

                parellel.getAll(keys);
                TestCase.fail("Should not have gotten this far");

            } catch (Exception e) {
                // expected behavior
            }

            try {

                parellel.getParallel(
                    new Callback<Object, ResourceEvent<String>>() {

                        @Override
                        public Object call(
                                ResourceEvent<String> value) {
                            return false;
                        }

                    },
                    null
                );
                TestCase.fail("Should not have gotten this far");

            } catch (Exception e) {
                // expected behavior
            }

            try {

                parellel.getParallel(null, "");
                TestCase.fail("Should not have gotten this far");

            } catch (Exception e) {
                // expected behavior
            }

            try {

                parellel.existsParallel(
                    new Callback<Object, ResourceEvent<Boolean>>() {

                        @Override
                        public Object call(
                                ResourceEvent<Boolean> value) {
                            return false;
                        }

                    },
                    null
                );
                TestCase.fail("Should not have gotten this far");

            } catch (Exception e) {
                // expected behavior
            }

            try {

                parellel.existsParallel(null, "");
                TestCase.fail("Should not have gotten this far");

            } catch (Exception e) {
                // expected behavior
            }
        }
    }
  
}
