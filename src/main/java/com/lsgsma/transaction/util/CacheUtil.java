package com.lsgsma.transaction.util;

import java.util.Arrays;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CacheUtil {

    public static String buildCacheKey(Object... parts) {
        return Arrays.stream(parts)
                .map(Object::toString)
                .collect(Collectors.joining("::"));
    }
}
