package llc.ufwa.data.resource.cache;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;

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
     * @return
     */
    public static final <Value extends Serializable> Cache<String, Value> getSerializingFileCache(
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
        
        return cache;
        
    }

}
