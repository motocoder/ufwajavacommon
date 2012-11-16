package llc.ufwa.javacommon.test.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import junit.framework.TestCase;
import llc.ufwa.concurrency.Callback;
import llc.ufwa.concurrency.ParallelControl;
import llc.ufwa.data.exception.ResourceException;
import llc.ufwa.data.resource.loader.DefaultResourceLoader;
import llc.ufwa.data.resource.loader.ParallelResourceLoader;
import llc.ufwa.data.resource.loader.ParallelResourceLoaderImpl;
import llc.ufwa.data.resource.loader.ResourceEvent;
import llc.ufwa.data.resource.loader.ResourceLoader;
import llc.ufwa.javacommon.test.JavaCommonLimitingExecutorService;

import org.apache.log4j.BasicConfigurator;
import org.junit.Test;

public class ForParellelResourceLoaderTest {

    static {
        BasicConfigurator.configure();
    }
    
    /**
     * This test just tests a simple get making sure the value returned is the correct one.
     */
    @Test
    public void testSimpleGet() {
        
        final ResourceLoader<String, String> internal = new DefaultResourceLoader<String, String>() {

            @Override
            public String get(String key) throws ResourceException {
                return key;
            }
            
        };
        
        final ParallelResourceLoader<String, String> parallelLoader = 
                new ParallelResourceLoaderImpl<String, String>(
                    internal,
                    new JavaCommonLimitingExecutorService(Executors.newFixedThreadPool(10),10),
                    Executors.newFixedThreadPool(10),
                    10,
                    ""
                );
        
        try {
            
            final String value = parallelLoader.get("Hi");
            
            TestCase.assertEquals("Hi", value);
            
        } 
        catch (ResourceException e) {
            
            e.printStackTrace();
            TestCase.fail("Should not have thrown exception");
            
        }
        
    }
    
    /**
     * This makes sure get fails when an exception is thrown.
     */
    @Test
    public void testSimpleGetException() {
        
        final ResourceLoader<String, String> internal = new DefaultResourceLoader<String, String>() {

            @Override
            public String get(String key) throws ResourceException {
                throw new ResourceException("blah");
            }
            
        };
        
        final ParallelResourceLoader<String, String> parallelLoader = 
                new ParallelResourceLoaderImpl<String, String>(
                    internal,
                    new JavaCommonLimitingExecutorService(Executors.newFixedThreadPool(10),10),
                    Executors.newFixedThreadPool(10),
                    10,
                    ""
                );
        
        try {
            
            parallelLoader.get("Hi");
            
            TestCase.fail("Should have thrown exception");
            
        } 
        catch (ResourceException e) {
            //correct behavior
        }
        
    }
    
    /**
     * This just tests exists to make sure the value returned is as expected.
     */
    @Test
    public void testSimpleExists() {
        
        final ParallelControl<Object> control = new ParallelControl<Object>();
        
        control.setValue(true);
        
        final ResourceLoader<String, String> internal = new DefaultResourceLoader<String, String>() {

            @Override
            public String get(String key) throws ResourceException {
                return key;
            }

            @Override
            public boolean exists(String key) throws ResourceException {
                return (Boolean)control.getValue();
            }
            
        };
        
        final ParallelResourceLoader<String, String> parallelLoader = 
            new ParallelResourceLoaderImpl<String, String>(
                internal,
                new JavaCommonLimitingExecutorService(Executors.newFixedThreadPool(10),10),
                Executors.newFixedThreadPool(10),
                10,
                ""
            );
        
        try {
            
            boolean value = parallelLoader.exists("Hi");
            
            TestCase.assertTrue(value);
            
            control.setValue(false);
            
            value = parallelLoader.exists("Hi");
            
            TestCase.assertFalse(value);
            
        } 
        catch (ResourceException e) {
            
            e.printStackTrace();
            TestCase.fail("Should not have thrown exception");
            
        }
        
    }
    
    /**
     * Tests exists method failure due to exception
     */
    @Test
    public void testSimpleExistsException() {
        
        final ResourceLoader<String, String> internal = new DefaultResourceLoader<String, String>() {

            @Override
            public String get(String key) throws ResourceException {
                throw new ResourceException("blah");
            }
            
        };
        
        final ParallelResourceLoader<String, String> parallelLoader = 
                new ParallelResourceLoaderImpl<String, String>(
                    internal, 
                    new JavaCommonLimitingExecutorService(Executors.newFixedThreadPool(10),10),
                    Executors.newFixedThreadPool(10),
                    10,
                    ""
                );
        
        try {
            
            parallelLoader.exists("Hi");
            
            TestCase.fail("Should have thrown exception");
            
        } 
        catch (ResourceException e) {
            //correct behavior
        }
        
    }
    
    /**
     * Tests getAll
     */
    @Test
    public void testSimpleGetAll() {
        
        final ResourceLoader<String, String> internal = new DefaultResourceLoader<String, String>() {

            @Override
            public String get(String key) throws ResourceException {
                return key;
            }
            
        };
        
        final ParallelResourceLoaderImpl<String, String> parallelLoader = 
                new ParallelResourceLoaderImpl<String, String>(
                    internal,
                    new JavaCommonLimitingExecutorService(Executors.newFixedThreadPool(100),10), 
                    Executors.newFixedThreadPool(10),
                    10,
                    ""
                );
        
        try {
            
            final List<String> keys = new ArrayList<String>();
            
            keys.add("Hi");
            keys.add("Hi2");
            keys.add("Hi3");
            keys.add("Hi3");
            keys.add("Hi2");
            
            System.out.println("Getting all");
            final List<String> values = parallelLoader.getAll(keys);
            System.out.println("Got all");
            for(int i = 0; i < values.size(); i++) {
                
                final String val = values.get(i);
                final String key = keys.get(i);
                
                TestCase.assertEquals(val, key);
                
            }
            
        } 
        catch (ResourceException e) {
            
            e.printStackTrace();
            TestCase.fail("Should not have thrown exception");
            
        }
        
    }
    
    /**
     * tests getAll for when it throws an exception.
     */
    @Test
    public void testFailingGetAll() {
        
        final ResourceLoader<String, String> internal = new DefaultResourceLoader<String, String>() {

            @Override
            public String get(String key) throws ResourceException {
                
                if(key.equals("Hi3")) {
                    throw new ResourceException("Failed");
                }
                return key;
            }
            
        };
        
        final ParallelResourceLoader<String, String> parallelLoader = 
                new ParallelResourceLoaderImpl<String, String>(
                    internal,
                    new JavaCommonLimitingExecutorService(Executors.newFixedThreadPool(10),10), 
                    Executors.newFixedThreadPool(10), 
                    10,
                    ""
                );
        
        try {
            
            final List<String> keys = new ArrayList<String>();
            
            keys.add("Hi");
            keys.add("Hi2");
            keys.add("Hi3");
            keys.add("Hi3");
            keys.add("Hi2");
            
            parallelLoader.getAll(keys);
            TestCase.fail("Should have thrown exception");
            
        } 
        catch (ResourceException e) {
            //expected behavior
        }
        
    }
    
    /**
     * tests getAll for when it throws an exception.
     */
    @Test
    public void testFailingGetAllRegularRuntimeException() {
        
        final ResourceLoader<String, String> internal = new DefaultResourceLoader<String, String>() {

            @Override
            public String get(String key) throws ResourceException {
                
                if(key.equals("Hi3")) {
                    throw new RuntimeException("Failed");
                }
                return key;
            }
            
        };
        
        final ParallelResourceLoader<String, String> parallelLoader = 
                new ParallelResourceLoaderImpl<String, String>(
                    internal,
                    new JavaCommonLimitingExecutorService(Executors.newFixedThreadPool(10),10),
                    Executors.newFixedThreadPool(10),
                    10,
                    ""
                );
        
        try {
            
            final List<String> keys = new ArrayList<String>();
            
            keys.add("Hi");
            keys.add("Hi2");
            keys.add("Hi3");
            keys.add("Hi3");
            keys.add("Hi2");
            
            parallelLoader.getAll(keys);
            TestCase.fail("Should have thrown exception");
            
        } 
        catch (ResourceException e) {
            //expected behavior
        }
        
    }
    
//    /**
//     * Makes sure the call fails when too many unique calls are already being made.
//     */
//    @Test
//    public void testFailingUnique() {
//        
//        final ResourceLoader<String, String> internal = new DefaultResourceLoader<String, String>() {
//
//            @Override
//            public String get(String key) throws ResourceException { 
//                
//                try {
//                    
//                    Thread.sleep(1000);
//                    return key;
//                    
//                } 
//                catch (InterruptedException e) {
//                    throw new ResourceException("error");
//                }
//                
//            }
//            
//        };
//        
//        final ParallelResourceLoaderImpl<String, String> parallelLoader = 
//                new ParallelResourceLoaderImpl<String, String>(
//                    internal,
//                    new JavaCommonLimitingExecutorService(Executors.newFixedThreadPool(10),1),
//                    Executors.newFixedThreadPool(10),
//                    10,
//                    1,
//                    ""
//                );
//        
//        try {
//            
//            final List<String> keys = new ArrayList<String>();
//            
//            keys.add("Hi");
//            keys.add("Hi2");
//            keys.add("Hi3");
//            keys.add("Hi3");
//            keys.add("Hi2");
//            parallelLoader.getParallel(new Callback<Object, ResourceEvent<String>>() {
//
//                @Override
//                public boolean call(Object source, ResourceEvent<String> value) { 
//                    return false;
//                }}, "Hi");
//            
//            parallelLoader.getAll(keys);
//            TestCase.fail("Should have thrown exception");
//            
//        } 
//        catch (ResourceException e) {
//            //expected behavior
//        }
//        
//    }
    
    /** 
     * makes sure the oldest callback is kicked off when too many callbacks are on one key.
     */
    @SuppressWarnings("rawtypes")
    @Test
    public void testFailingDepth() {
        
        final ResourceLoader<String, String> internal = new DefaultResourceLoader<String, String>() {

            @Override
            public String get(String key) throws ResourceException { 
                
                if(key.equals("block")) {
                    
                    try {
                        Thread.sleep(2000);
                    } 
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    
                }
                
                return key;
            }
            
        };

        
        final ParallelResourceLoader<String, String> parallelLoader = 
                new ParallelResourceLoaderImpl<String, String>(
                    internal, 
                    new JavaCommonLimitingExecutorService(Executors.newFixedThreadPool(10),10),
                    Executors.newFixedThreadPool(10),
                    1,
                    ""
                );
        
        try {
            
            final ParallelControl<Object> control1 = new ParallelControl<Object>();
            
            final List<String> keys = new ArrayList<String>();
            
            keys.add("Hi");
            keys.add("Hi2");
            keys.add("Hi3");
            keys.add("Hi3");
            keys.add("Hi2");
            keys.add("block");
            
            parallelLoader.getParallel(
                new Callback<Object, ResourceEvent<String>>() {
    
                    @Override
                    public boolean call(Object source, ResourceEvent<String> value) {
                        
                        control1.setValue(value);
                        control1.unBlockOnce();
                        
                        return false;
                    }
                    
                },
                "block"
            );
            
            parallelLoader.getAll(keys);
            
            try {
                control1.blockOnce();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
            TestCase.assertNotNull(((ResourceEvent)control1.getValue()).getThrowable());
            
        } 
        catch (ResourceException e) {
            //expected behavior
        }
        
    }
    
    /**
     * Tests the depth property for failure.
     */
    @SuppressWarnings("rawtypes")
    @Test
    public void testOtherFailingDepth() {
        
        final ResourceLoader<String, String> internal = new DefaultResourceLoader<String, String>() {

            @Override
            public String get(String key) throws ResourceException { 
                
                if(key.equals("block")) {
                    
                    try {
                        Thread.sleep(2000);
                    } 
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    
                }
                
                return key;
            }
            
        };

        
        final ParallelResourceLoaderImpl<String, String> parallelLoader = 
                new ParallelResourceLoaderImpl<String, String>(
                    internal,
                    new JavaCommonLimitingExecutorService(Executors.newFixedThreadPool(10),10), 
                    Executors.newFixedThreadPool(10),
                    1,
                    ""
                );

        final ParallelControl<Object> control1 = new ParallelControl<Object>();
        
        new Thread() {
            
            public void run() {
                
                final List<String> keys = new ArrayList<String>();
                
                keys.add("Hi");
                keys.add("Hi2");
                keys.add("Hi3");
                keys.add("Hi3");
                keys.add("Hi2");
                keys.add("block");
                
                try {
                    
                    control1.unBlockOnce();
                    
                    final List<String> val = parallelLoader.getAll(keys);
                    
                    control1.setValue(val);
                    
                } 
                catch (ResourceException e) {
                    control1.setValue(e);
                }
                
                control1.unBlockOnce();
                  
            }
            
        }.start();
        
        final ParallelControl<Object> control2 = new ParallelControl<Object>();
        
        try {
            
            control1.blockOnce();
            Thread.sleep(100);
            
        }
        catch (InterruptedException e1) {
            TestCase.fail();
        }
        
        parallelLoader.getParallel(
            new Callback<Object, ResourceEvent<String>>() {

                @Override
                public boolean call(Object source, ResourceEvent<String> value) {
                   
                    control2.setValue(value);
                    control2.unBlockOnce();
                    
                    return false;  
                }
                
            },
            "block"
        );
        
        try {
            control1.blockOnce();
            control2.blockOnce();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
                
        TestCase.assertTrue(control1.getValue() instanceof Exception);
        TestCase.assertNotNull(((ResourceEvent)control2.getValue()).getVal());
        
    }
    
}
