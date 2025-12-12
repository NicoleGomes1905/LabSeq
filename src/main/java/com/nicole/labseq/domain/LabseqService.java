package com.nicole.labseq.domain;

import jakarta.enterprise.context.ApplicationScoped;
import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@ApplicationScoped
public class LabseqService {

    private final Map<Long, BigInteger> memo = new ConcurrentHashMap<>();
    private final AtomicLong computedUpTo = new AtomicLong(-1);

    public LabseqService() {
        memo.put(0L, BigInteger.ZERO);
        memo.put(1L, BigInteger.ONE);
        memo.put(2L, BigInteger.ZERO);
        memo.put(3L, BigInteger.ONE);
        computedUpTo.set(3);
    }

    public BigInteger value(long n) {
        if (n < 0) {
            throw new IllegalArgumentException("Index must be a non-negative integer.");
        }
        BigInteger cached = memo.get(n);
        if (cached != null) {
            return cached;
        }
        synchronized (this) {
            long start = computedUpTo.get() + 1;
            if (start < 0) start = 0;
            for (long i = start; i <= n; i++) {
                if (i <= 3) continue; // bases jÃƒÂ¡ preenchidas
                BigInteger li = memo.get(i - 3).add(memo.get(i - 4));
                memo.put(i, li);
            }
            computedUpTo.set(Math.max(computedUpTo.get(), n));
        }
        return memo.get(n);
    }
}
