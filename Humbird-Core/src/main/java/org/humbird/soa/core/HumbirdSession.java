package org.humbird.soa.core;

import net.sf.ehcache.util.TimeUtil;

import java.io.Serializable;

/**
 * Created by david on 15/3/19.
 */
public class HumbirdSession implements Serializable {

    private int EXPIRATION = 0;

    private Object key;

    private Object value;

    public HumbirdSession(Object key, Object value) {
        this(0);
        this.key = key;
        this.value = value;
    }

    public HumbirdSession(int EXPIRATION) {
        this.EXPIRATION = EXPIRATION;
        this.timeToLive = EXPIRATION == 0 ? -2147483648 : EXPIRATION;
        this.timeToIdle = -2147483648;
        this.creationTime = this.getCurrentTime();
        this.cacheDefaultLifespan = true;
    }


    private volatile int timeToLive;

    private volatile int timeToIdle;

    private transient long creationTime;

    private transient long lastAccessTime;

    private volatile long lastUpdateTime;

    private volatile boolean cacheDefaultLifespan;

    public int getEXPIRATION() {
        return EXPIRATION;
    }

    public void setEXPIRATION(int EXPIRATION) {
        this.EXPIRATION = EXPIRATION;
    }

    public boolean isLifespanSet() {
        return this.timeToIdle != -2147483648 || this.timeToLive != -2147483648;
    }

    public long getExpirationTime() {
        if(this.isLifespanSet() && !this.isEternal()) {
            long expirationTime = 0L;
            long ttlExpiry = this.creationTime + TimeUtil.toMillis(this.getTimeToLive());
            long mostRecentTime = Math.max(this.creationTime, this.lastAccessTime);
            long ttiExpiry = mostRecentTime + TimeUtil.toMillis(this.getTimeToIdle());
            if(this.getTimeToLive() != 0 && (this.getTimeToIdle() == 0 || this.lastAccessTime == 0L)) {
                expirationTime = ttlExpiry;
            } else if(this.getTimeToLive() == 0) {
                expirationTime = ttiExpiry;
            } else {
                expirationTime = Math.min(ttlExpiry, ttiExpiry);
            }

            return expirationTime;
        } else {
            return 9223372036854775807L;
        }
    }

    public int getTimeToLive() {
        return -2147483648 == this.timeToLive?0:this.timeToLive;
    }

    public int getTimeToIdle() {
        return -2147483648 == this.timeToIdle?0:this.timeToIdle;
    }

    public boolean isEternal() {
        return 0 == this.timeToIdle && 0 == this.timeToLive;
    }

    long getCurrentTime() {
        return System.currentTimeMillis();
    }

    public boolean isExpired() {
        if(this.isLifespanSet() && !this.isEternal()) {
            long now = this.getCurrentTime();
            long expirationTime = this.getExpirationTime();
            return now > expirationTime;
        } else {
            return false;
        }
    }

    public void setTimeToLive(int timeToLiveSeconds) {
        if(timeToLiveSeconds < 0) {
            throw new IllegalArgumentException("timeToLive can\'t be negative");
        } else {
            this.cacheDefaultLifespan = false;
            this.timeToLive = timeToLiveSeconds;
        }
    }

    public Object getKey() {
        return key;
    }

    public void setKey(Object key) {
        this.key = key;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "HumbirdSession{key='" + this.key + "', value='" + this.value +"'}";
    }
}
