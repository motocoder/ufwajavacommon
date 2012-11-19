package llc.ufwa.javacommon.test.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import junit.framework.TestCase;
import llc.ufwa.concurrency.Callback;
import llc.ufwa.concurrency.ParallelControl;
import llc.ufwa.data.exception.CanceledResourceException;
import llc.ufwa.data.exception.ResourceException;
import llc.ufwa.data.resource.loader.DefaultResourceLoader;
import llc.ufwa.data.resource.loader.ParallelResourceLoader;
import llc.ufwa.data.resource.loader.ParallelResourceLoaderImpl;
import llc.ufwa.data.resource.loader.ResourceEvent;
import llc.ufwa.data.resource.loader.ResourceLoader;
import llc.ufwa.javacommon.test.JavaCommonLimitingExecutorService;

import org.apache.log4j.BasicConfigurator;
import org.junit.Test;

public class ParellelResourceLoaderTest {
    
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
         
        final ParallelResourceLoader<String, String> parellelLoader = 
            new ParallelResourceLoaderImpl<String, String>(
                internal, 
                new JavaCommonLimitingExecutorService(Executors.newFixedThreadPool(10),10),
                Executors.newFixedThreadPool(10), 
                10,
                ""
            );
        
        {
            
            final Callback<Object, ResourceEvent<String>> callback = new Callback<Object, ResourceEvent<String>>() {
    
                @Override
                public boolean call(Object source, ResourceEvent<String> value) {
                    
                    control.setValue(value);
                    control.unBlockOnce();
                    
                    return false;
                }
                
            };
                
            try {
                parellelLoader.getParallel(callback, "Hi");
            }
            catch (ResourceException e1) {
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
                public boolean call(Object source, ResourceEvent<Boolean> value) {
                    
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
        
        final ParallelResourceLoader<String, String> parellelLoader = 
            new ParallelResourceLoaderImpl<String, String>(
                internal,
                new JavaCommonLimitingExecutorService(Executors.newFixedThreadPool(10),10),
                Executors.newFixedThreadPool(10),
                1,
                ""
                );
        
        {
            
            final Callback<Object, ResourceEvent<String>> callback1 = new Callback<Object, ResourceEvent<String>>() {
    
                @Override
                public boolean call(Object source, ResourceEvent<String> value) {
                    
                    control1.setValue(value);
                    control1.unBlockOnce();
                    
                    return false;
                }
                
            };
            
            final Callback<Object, ResourceEvent<String>> callback2 = new Callback<Object, ResourceEvent<String>>() {
                
                @Override
                public boolean call(Object source, ResourceEvent<String> value) {
                    
                    control2.setValue(value);
                    control2.unBlockOnce();
                    
                    return false;
                }
                
            };
            
            try {  
                parellelLoader.getParallel(callback1, "Hi");            
            
                parellelLoader.getParallel(callback2, "Hi");
            }
            catch (ResourceException e1) {
                TestCase.fail();
            }
            
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
            
            
            System.out.println(val1.getThrowable());
            TestCase.assertTrue(val1.getThrowable() instanceof CanceledResourceException);
            
        }
        
    }
    
//    @SuppressWarnings("unchecked")
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
//            final ParallelResourceLoader<String, String> parellelLoader = 
//                    new ParallelResourceLoaderImpl<String, String>(
//                        internal,
//                        new JavaCommonLimitingExecutorService(Executors.newFixedThreadPool(10),1),
//                        Executors.newFixedThreadPool(10), 
//                        10, 
//                        1,
//                        ""
//                    );
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
//                try {
//                    parellelLoader.getParallel(callback1, "Hi");
//                
//                    parellelLoader.getParallel(callback2, "Hi2");
//                    
//                } catch (ResourceException e1) {
//                    TestCase.fail();
//                }
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
//                new ParallelResourceLoaderImpl<String, String>(internal, new JavaCommonLimitingExecutorService(Executors.newFixedThreadPool(10),1), Executors.newFixedThreadPool(10), 10, 1, "");
//            
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

        final ParallelResourceLoader<String, String> parellelLoader =
            new ParallelResourceLoaderImpl<String, String>(
                internal, 
                new JavaCommonLimitingExecutorService(Executors.newFixedThreadPool(10),10),
                Executors.newFixedThreadPool(10),
                10, 
                ""
            );

        {

            final Callback<Object, ResourceEvent<String>> callback = new Callback<Object, ResourceEvent<String>>() {

                @Override
                public boolean call(Object source, ResourceEvent<String> value) {

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
                TestCase.fail();
            }

            TestCase.assertNotNull(((ResourceEvent<String>) control.getValue()).getThrowable());

        }

        {

            final Callback<Object, ResourceEvent<Boolean>> callback = new Callback<Object, ResourceEvent<Boolean>>() {

                @Override
                public boolean call(Object source, ResourceEvent<Boolean> value) {

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
            
            new ParallelResourceLoaderImpl<String, String>(
                null, new JavaCommonLimitingExecutorService(Executors.newFixedThreadPool(10),10),
                Executors.newFixedThreadPool(10),
                10, 
                ""
            );
            TestCase.fail("Should not have gotten here");
        }
        catch(Exception e) {
            //expected
        }

        try {// null executors
            
            new ParallelResourceLoaderImpl<String, String>(
                internal,
                new JavaCommonLimitingExecutorService(Executors.newFixedThreadPool(10),10),
                null,
                10,
                ""
            );
            TestCase.fail("Should not have gotten here");
            
        }
        catch(Exception e) {
            //expected
        }
        
        try { // bad depth
            
            new ParallelResourceLoaderImpl<String, String>(
                internal,
                new JavaCommonLimitingExecutorService(Executors.newFixedThreadPool(10),10), 
                Executors.newFixedThreadPool(10), 
                0,
                ""
            );
            TestCase.fail("Should not have gotten here");
            
        }
        catch(Exception e) {
            //expected
        }
        
        try {// null loggingTag
            
            new ParallelResourceLoaderImpl<String, String>(
                internal,
                new JavaCommonLimitingExecutorService(Executors.newFixedThreadPool(10),10),
                Executors.newFixedThreadPool(10),
                10,
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

        {// null internal

            final ParallelResourceLoaderImpl<String, String> parellel =
                new ParallelResourceLoaderImpl<String, String>(
                    internal, 
                    new JavaCommonLimitingExecutorService(Executors.newFixedThreadPool(10),10),
                    Executors.newFixedThreadPool(10),
                    10,
                    ""
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
                        public boolean call(Object source,
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
                        public boolean call(Object source,
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
    
    private int count;
    
//    @Test
//    public void testConcurrentCalls() {
//        
//        count = 0;
//        
//        final ParallelControl<Object> loaderControl = new ParallelControl<Object>();
//        
//        final Set<String> out = new HashSet<String>();
//        
//        
//        final ResourceLoader<String, String> internal = new DefaultResourceLoader<String, String>() {
//            
//            @Override
//            public String get(String key) throws ResourceException {
//                
//                synchronized(out) {
//                    if(out.contains(key)) {
//                        System.out.println("duplicates out");
//                    }
//                    out.add(key);
//                }
//                count++;
//                try {
//                    Thread.sleep(100);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                
//                synchronized(out) {
//                    out.remove(key);
//                }
////                loaderControl.blockOnce();
//                
//                return key;
//                
//            }
//            
//        };
//        
//        final Cache<String, String> cache = new SynchronizedCache<String, String>(new ExpiringCache<String, String>(new MemoryCache<String, String>(), 2, 1000));
//        final Cache<String, Boolean> searchCache = new SynchronizedCache<String, Boolean>(new ExpiringCache<String, Boolean>(new MemoryCache<String, Boolean>(), 2, 1000));
//        
//        final CachedParallelResourceLoader<String, String> parellel =
//            new CachedParallelResourceLoader<String, String>(
//                internal, 
//                new JavaCommonLimitingExecutorService(Executors.newFixedThreadPool(10),10),
//                Executors.newFixedThreadPool(1000),
//                Executors.newFixedThreadPool(10), 
//                50, 
//                "",
//                cache, 
//                searchCache
//            );
//        
//        final List<String> keys = new ArrayList<String>();
//        
//        keys.add("hi");
//        keys.add("hi2");
//        keys.add("hi3");
//        keys.add("hi4");
//        keys.add("hi5");
//        keys.add("hi6");
//        keys.add("hi7");
//        keys.add("hi8");
//        keys.add("hi9");
//        keys.add("hi6");
//        keys.add("hi7");
//        keys.add("hi8");
//        keys.add("hi9");
//        keys.add("hi");
//        keys.add("hi2");
//        keys.add("hi3");
//        keys.add("hi4");
//        keys.add("hi5");
//        keys.add("hi6");
//        keys.add("hi7");
//        keys.add("hi8");
//        keys.add("hi9");
//        keys.add("hi6");
//        keys.add("hi7");
//        keys.add("hi8");
//        keys.add("hi9");
//        keys.add("hi");
//        keys.add("hi2");
//        keys.add("hi3");
//        keys.add("hi4");
//        keys.add("hi5");
//        keys.add("hi6");
//        keys.add("hi7");
//        keys.add("hi8");
//        keys.add("hi9");
//        keys.add("hi6");
//        keys.add("hi7");
//        keys.add("hi8");
//        keys.add("hi9");
//        
//        for(int i = 0; i < 10; i++) {
////        while(true) {
//            
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//            
//            new Thread() {
//                @Override 
//                public void run() {
//                
//                    final Map<String, Callback<Object, ResourceEvent<String>>> callbacks = new HashMap<String, Callback<Object, ResourceEvent<String>>>();
//                    
//                    for(final String key : keys) {
//                        
//                        callbacks.put(key,
//                            new Callback<Object, ResourceEvent<String>>() {
//
//                                @Override
//                                public boolean call(
//                                    final Object source,
//                                    final ResourceEvent<String> value
//                                ) {
//                                    return false;
//                                }
//                            }
//                        );  
//                        
//                    }
//                    
//                    try {
//						parellel.getAllParallel(callbacks);
//					} 
//                    catch (ResourceException e) {
//						TestCase.fail();
//					}
// 
//                }
//            }.start();
//            
//            new Thread() {
//                @Override
//                public void run() {
//                    
//                    for(final String key : keys) {
//                        
//                        parellel.getParallel(
//                            new Callback<Object, ResourceEvent<String>>() {
//
//                            @Override
//                            public boolean call(
//                                final Object source, 
//                                final ResourceEvent<String> value
//                            ) {
//                                return false;
//                            }
//                        },
//                        key
//                        );
//                        
//                    }
//                   
//                }
//            }.start();
//               
//        }
        
//        try {
//            Thread.sleep(1000);
//        } 
//        catch (InterruptedException e) {
//            
//            e.printStackTrace();
//            
//            TestCase.fail("shouldn't get interrupted");
//            
//        }
//        
//        loaderControl.unBlockAll();
//        
//        System.out.println("count " + count);
//        
//        TestCase.assertEquals(2, count);
        
        
//    }
  
}
