package llc.ufwa.data.resource.cache;

import java.io.File;
import java.io.InputStream;

import llc.ufwa.concurrency.Callback;
import llc.ufwa.data.resource.Converter;
import llc.ufwa.data.resource.InputStreamConverter;
import llc.ufwa.data.resource.ReverseConverter;
import llc.ufwa.data.resource.SerializingConverter;

public class CacheFactory {
    
    /**
     * 
     * @param maxSize
     * @param expireTimeout
     * @param cacheRoot - must be unique to this cache. Can not be any other cache's root directory.
     * @param sizeConverter
     * @return
     */
    public static final <Value> Cache<String, Value> getSerializingFileCache(
        final int maxSize,
        final int expireTimeout,
        final File cacheRoot,
        final Converter<Integer, Value> sizeConverter
    ) {
        
        final File dataFolder = new File(cacheRoot, "data");
        final File tempFolder = new File(cacheRoot, "temp");
        
        final File expiringRoot = new File(cacheRoot, "expiringRoot");
        
        final File expiringDataFolder = new File(expiringRoot, "data");
        final File expiringTempFolder = new File(expiringRoot, "temp");
        
        final FileHashCache diskCache = new FileHashCache(dataFolder, tempFolder);
        
        final ValueConvertingCache<String, Value, byte[]> fileCache = 
            new ValueConvertingCache<String, Value, byte []>(
                new ValueConvertingCache<String, byte [], InputStream>(
                        diskCache,
                        new ReverseConverter<byte [], InputStream>(new InputStreamConverter())
                    ),
                    new SerializingConverter<Value>()
                );
            
        final FileHashCache expringPersistDiskCache = new FileHashCache(expiringDataFolder, expiringTempFolder);
            
        final Cache<String, Value> cache = 
            new FilePersistedExpiringCache<Value>(                    
                new FilePersistedMaxSizeCache<Value>(
                    dataFolder,
                    fileCache,
                    sizeConverter,
                    maxSize
                ),
                expringPersistDiskCache,
                (long)expireTimeout,
                (long)(expireTimeout * 2)
            );
        
        return new SynchronizedCache<String, Value>(cache);
        
    }
    
    public static final <Value> Cache<String, Value> getSerializingMaxCountFileCache(
        final int maxCount,
        final File cacheRoot,
        final Callback<Void, Value> onRemoved
    ) {
        
        final File dataFolder = new File(cacheRoot, "data");
        final File tempFolder = new File(cacheRoot, "temp");
        
        final FileHashCache diskCache = new FileHashCache(dataFolder, tempFolder);
        
        final ValueConvertingCache<String, Value, byte[]> fileCache = 
            new ValueConvertingCache<String, Value, byte []>(
                new ValueConvertingCache<String, byte [], InputStream>(
                        diskCache,
                        new ReverseConverter<byte [], InputStream>(new InputStreamConverter())
                    ),
                    new SerializingConverter<Value>()
                );
            
        final Cache<String, Value> cache =                
                new FilePersistedMaxCountCache<Value>(
                    dataFolder,
                    fileCache,
                    maxCount,
                    onRemoved
                );
        
        return new SynchronizedCache<String, Value>(cache);
        
    }
    
    /**
     * 
     * @param maxSize
     * @param cacheRoot - must be unique to this cache. Can not be any other cache's root directory.
     * @param sizeConverter
     * @return
     */
    public static final <Value> Cache<String, Value> getMaxSizeFileCache(
    	final long maxSize,
        final File cacheRoot,
        final Converter<Integer, Value> sizeConverter
    ) {
        
        final File dataFolder = new File(cacheRoot, "data");
        final File tempFolder = new File(cacheRoot, "temp");
        
        final FileHashCache diskCache = new FileHashCache(dataFolder, tempFolder);
        
        final ValueConvertingCache<String, Value, byte[]> fileCache = 
            new ValueConvertingCache<String, Value, byte []>(
                new ValueConvertingCache<String, byte [], InputStream>(
                        diskCache,
                        new ReverseConverter<byte [], InputStream>(new InputStreamConverter())
                    ),
                    new SerializingConverter<Value>()
                );
        
        final Cache<String, Value> cache =                
                new FilePersistedMaxSizeCache<Value>(
                    dataFolder,
                    fileCache,
                    sizeConverter,
                    maxSize
                );
        
        return new SynchronizedCache<String, Value>(cache);
        
    }
    
    /**
     * 
     * @param expireTimeout
     * @param cacheRoot - must be unique to this cache. Can not be any other cache's root directory.
     * @param sizeConverter
     * @return
     */
    public static final <Value> Cache<String, Value> getExpiringFileCache(
        final long expireTimeout,
        final File cacheRoot,
        final Converter<Integer, Value> sizeConverter
    ) {
        
    	final File dataFolder = new File(cacheRoot, "data");
        final File tempFolder = new File(cacheRoot, "temp");
        
        final File expiringRoot = new File(cacheRoot, "expiringRoot");
        
        final File expiringDataFolder = new File(expiringRoot, "data");
        final File expiringTempFolder = new File(expiringRoot, "temp");
        
        final FileHashCache diskCache = new FileHashCache(dataFolder, tempFolder);
        
        final ValueConvertingCache<String, Value, byte[]> fileCache = 
            new ValueConvertingCache<String, Value, byte []>(
                new ValueConvertingCache<String, byte [], InputStream>(
                        diskCache,
                        new ReverseConverter<byte [], InputStream>(new InputStreamConverter())
                    ),
                    new SerializingConverter<Value>()
                );
            
        final FileHashCache expringPersistDiskCache = new FileHashCache(expiringDataFolder, expiringTempFolder);
            
        final Cache<String, Value> cache = 
            new FilePersistedExpiringCache<Value>(                    
                fileCache,
                expringPersistDiskCache,
                expireTimeout,
                expireTimeout
            );
        
        return new SynchronizedCache<String, Value>(cache);
        
    }
    
    /**
     * 
     * @param maxSize
     * @param expireTimeout
     * @param cacheRoot - must be unique to this cache. Can not be any other cache's root directory.
     * @param sizeConverter
     * @return
     */
    public static final <Value> Cache<String, Value> getHashCashFileCache(
        final File cacheRoot
    ) {
        
        final File dataFolder = new File(cacheRoot, "data");
        final File tempFolder = new File(cacheRoot, "temp");
        
        final FileHashCache diskCache = new FileHashCache(dataFolder, tempFolder);
        
        final ValueConvertingCache<String, Value, byte[]> fileCache = 
            new ValueConvertingCache<String, Value, byte []>(
                new ValueConvertingCache<String, byte [], InputStream>(
                        diskCache,
                        new ReverseConverter<byte [], InputStream>(new InputStreamConverter())
                    ),
                    new SerializingConverter<Value>()
                );
        
        return new SynchronizedCache<String, Value>(fileCache);
        
    }
    
    public static final <Value> Cache<String, Value> getHashCashFileCache(
			final File cacheRoot,
	        final Converter<byte[], InputStream> valueConvertBytesToStream2, 
	        final Converter<Value, byte[]> valueConvertListToBytes
    ) {
		
		final File dataFolder = new File(cacheRoot, "data");
        final File tempFolder = new File(cacheRoot, "temp");
        
        final FileHashCache diskCache = new FileHashCache(dataFolder, tempFolder);
		
        final Cache<String, Value> listCache = 
            	new SynchronizedCache<String, Value>(
            		new ValueConvertingCache<String, Value, byte[]>(
            			new ValueConvertingCache<String, byte[], InputStream>(
            				new KeyEncodingCache<InputStream>(
            					diskCache
            				),
            				valueConvertBytesToStream2
            			),
            			valueConvertListToBytes
            		)
            	);
        
        return listCache;
        
	}

	public static final <Value> Cache<Long, Value> getHashCashFileCacheLong(
			final File cacheRoot,
	        final Converter<Long, String> sizeConverter, 
	        final Converter<byte[], InputStream> valueConvertBytesToStream, 
	        final Converter<Long, String> keyConvertLongToString, 
	        final Converter<Value, byte[]> valueConvertJobToBytes) {
		
		final File dataFolder = new File(cacheRoot, "data");
        final File tempFolder = new File(cacheRoot, "temp");
        
        final FileHashCache diskCache = new FileHashCache(dataFolder, tempFolder);
		
        final Cache<Long, Value> converted = 
    			new SynchronizedCache<Long, Value> (
    					new KeyConvertingCache<Long, String, Value> (
    						new ValueConvertingCache<String, Value, byte[]> (
    							new ValueConvertingCache<String, byte[], InputStream> (
    								diskCache,
    								valueConvertBytesToStream
    							),
    							valueConvertJobToBytes
    						),
    						keyConvertLongToString
    					)
    				);
        
        return converted;
        
	}
	
	public static final <Value> Cache<String, Value> getMaxSizeExpiringFileCache(
        final File cacheRoot,
        final int maxSize,
        final int expireTimeout,
        final Converter<Integer, Value> sizeConverter, 
        final Converter<byte[], InputStream> valueConvertBytesToStream,
        final Converter<Value, byte[]> valueConvertJobToBytes
    ) {
        
        final File dataFolder = new File(cacheRoot, "data");
        final File tempFolder = new File(cacheRoot, "temp");
        
        final File expiringRoot = new File(cacheRoot, "expiringRoot");
        
        final File expiringDataFolder = new File(expiringRoot, "data");
        final File expiringTempFolder = new File(expiringRoot, "temp");
        
        final FileHashCache diskCache = new FileHashCache(dataFolder, tempFolder);
        
        final Cache<String, Value> fileCache = 
            new SynchronizedCache<String, Value> (
                    new ValueConvertingCache<String, Value, byte[]> (
                        new ValueConvertingCache<String, byte[], InputStream> (
                            diskCache,
                            valueConvertBytesToStream
                        ),
                        valueConvertJobToBytes
                    )
                
            );
            
        final FileHashCache expringPersistDiskCache = new FileHashCache(expiringDataFolder, expiringTempFolder);
            
        final Cache<String, Value> cache = 
            new FilePersistedExpiringCache<Value>(     
                new FilePersistedMaxSizeCache<Value>(
                    dataFolder,
                    fileCache,
                    sizeConverter,
                    maxSize
                ),
                expringPersistDiskCache,
                (long)expireTimeout,
                (long)(expireTimeout * 2)
            );
        
        return new SynchronizedCache<String, Value>(cache);
                
    }

}
