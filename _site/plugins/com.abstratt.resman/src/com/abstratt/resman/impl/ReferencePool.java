package com.abstratt.resman.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.core.runtime.Assert;

import com.abstratt.pluginutils.LogUtils;
import com.abstratt.resman.ResourceManager;

/**
 * A reference pool where references are cleaned-up as they pass their expiration time.
 */
public class ReferencePool<K, R> {
	/**
	 * Protocol for disposing of garbage collected pooled references.
	 */
	public static interface ReferenceDisposer<R> {
		void dispose(R toDispose);
	}
	/**
	 * A bucket groups multiple references for the same key.
	 * 
	 * Clients may install multiple references for a same key for the sake of availability.
	 */
	private class Bucket {
		final private K key;
		public Bucket(K key) {
			this.key = key;
		}
		private Map<R, PooledReference> references = new IdentityHashMap<R, PooledReference>();
		public synchronized void add(R referent) {
			debug("Adding " + referent + " in " + key);
			final PooledReference newReference = new PooledReference(referent, initialLease);
			Assert.isTrue(newReference.acquire());
			references.put(referent, newReference);
		}
		public synchronized void release(R referent) {
			debug("Releasing " + referent + " in " + key);
			PooledReference reference = references.get(referent);
			if (reference != null) {
				reference.release();
				this.notify();
			}
		}
		public synchronized void forget(R referent) {
			debug("Forgetting " + referent + " in " + key);
			// all is being asked from us is that we no longer track the referent
			references.remove(referent);
		    this.notify();
		}
		public synchronized void remove() {
			debug("Removing bucket for " + this.key);
			for (PooledReference reference : references.values())
				dispose(reference);
			references.clear();
		}
		public synchronized R acquire() {
			debug("Trying to acquire " + key);
			int existing = references.size();
			// attempt at least once
			int attempts = 1 + existing / 3;
			for (int i = 0; i < attempts || existing >= bucketCap; i++) {
				if (i > 0 && !delay())
					break;
				List<PooledReference> values = new ArrayList<ReferencePool<K,R>.PooledReference>(references.values());
				Collections.shuffle(values);
				for (PooledReference reference : values)
					if (reference.acquire()) {
						final R acquired = reference.get();
						debug("Successfully acquired " + key + " - " + acquired);
						return acquired;
					}
			}
			debug("Could not acquire " + key);
			return null;
		}
		private boolean delay() {
			try {
				debug("Waiting for a reference");
				this.wait(100);
				return true;
			} catch (InterruptedException e) {
				return false;
			}
		}
		public void removeStaleReferences() {
			Collection<PooledReference> toDispose = new HashSet<PooledReference>();
			synchronized (this) {
				for (Iterator<Map.Entry<R, PooledReference>> it = references.entrySet().iterator(); it.hasNext();) {
				    Entry<R, PooledReference> next = it.next();
					final PooledReference reference = next.getValue();
					if (reference.isStale()) {
						toDispose.add(reference);
						it.remove();
						reference.invalidate();
					}
				}
			}
			for (PooledReference pooledReference : toDispose)
			    dispose(pooledReference);    	
		}
    }
	private class PooledReference {
		private final AtomicBoolean inUse = new AtomicBoolean();
		private final AtomicBoolean valid = new AtomicBoolean(true);
		private final R referent;
		private final AtomicLong expiry = new AtomicLong();
		public PooledReference(R referent, long initialLease) {
			this.referent = referent;
			this.expiry.set(System.currentTimeMillis() + initialLease * 1000);
		}
		public void invalidate() {
			inUse.set(true);
			valid.set(false);
		}
		public R get() {
			return referent;
		}
		private void renew(long renewal) {
			this.expiry.set(System.currentTimeMillis() + renewal * 1000);
		}
		private boolean isStale() {
			return !inUse.get() && expiry.get() < System.currentTimeMillis();
		}
		public boolean acquire() {
			// if we don't acquire, still register interest
			renew(renewal);
			return !inUse.getAndSet(true);
		}
		public void release() {
			renew(renewal);
			inUse.set(false);
		}
	}
	private static final boolean DEBUG_POOL = Boolean.getBoolean("com.abstratt.resman.debug.pool");
	private Map<K, Bucket> buckets = new HashMap<K, Bucket>();
	final private long initialLease;
	final private long renewal;
	final private int bucketCap;
	final private ReferenceDisposer<R> disposer;
	
	ReferencePool(int bucketCap, long lease, long renewal, ReferenceDisposer<R> disposer) {
		this.initialLease = lease;
		this.renewal = renewal;
		this.disposer = disposer;
		this.bucketCap = bucketCap;
		debug("Initialized reference pool");
		debug("\tbucketCap=" + bucketCap);
	}
	
	public ReferencePool(int bucketCap, long lease, ReferenceDisposer<R> disposer) {
		this(bucketCap, lease, lease, disposer);
	}
	
	public R add(K key, R referent) {
		Bucket bucket; 
		synchronized (this) {
			bucket = getBucket(key);
			if (bucket == null)
				buckets.put(key, bucket = new Bucket(key));
		}
		bucket.add(referent);
		removeStaleReferences();
		return referent;
	}

	private Bucket getBucket(K key) {
		return buckets.get(key);
	}

	public void release(K key, R referent) {
		synchronized (this) {
			Bucket bucket = getBucket(key);
			if (bucket != null)
				bucket.release(referent);
		}
		removeStaleReferences();
	}
	
	/**
	 * Forgets the existence of the referent. Responsibility for disposing the referent lies with the caller.
	 * @param key
	 * @param referent
	 */
	public void forget(K key, R referent) {
		synchronized (this) {
			Bucket bucket = getBucket(key);
			if (bucket != null)
				bucket.forget(referent);
		}
		removeStaleReferences();
	}
	/**
	 * Removes the entire bucket.
	 */
	public synchronized void remove(K key) {
		Bucket bucket = getBucket(key);
		if (bucket != null)
			bucket.remove();
	}

	
	private void removeStaleReferences() {
		Collection<Bucket> bucketCopies;
		synchronized (this) {
			bucketCopies = new HashSet<Bucket>(buckets.values()); 
		}
		for (Bucket bucket : bucketCopies)
			bucket.removeStaleReferences();
	}

	private void dispose(PooledReference toDispose) {
		if (disposer != null)
			disposer.dispose(toDispose.get());
	}

	public R acquire(K key) {
		Bucket bucket;
		synchronized (this) {
			bucket = getBucket(key);
		}
		if (bucket == null) {
			debug("No bucket for " + key);
			return null;
		}
		return bucket.acquire();
	}

	public void debug(final String message) {
		if (DEBUG_POOL) {
		    LogUtils.logInfo(ResourceManager.ID, message, null);
		}
	}

	public synchronized void clear() {
		buckets.clear();
	}

	public synchronized boolean isEmpty() {
		return buckets.isEmpty();
	}
}