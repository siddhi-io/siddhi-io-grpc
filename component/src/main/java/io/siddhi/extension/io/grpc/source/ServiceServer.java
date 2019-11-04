package io.siddhi.extension.io.grpc.source;

import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import io.siddhi.core.stream.input.source.Source;
import org.apache.log4j.Logger;

import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This is an abstract class extended by GrpcEventServiceServer and GenericServiceServer. This provides most of
 * initialization implementations common for both Service classes.
 */
public abstract class ServiceServer {
    protected boolean paused;
    protected ReentrantLock lock;
    protected Condition condition;


    protected abstract void setServerPropertiesToBuilder(String siddhiAppName, String streamID);
    protected abstract void addServicesAndBuildServer(String siddhiAppName, String streamID);
    protected abstract void connectServer(Logger logger, Source.ConnectionCallback connectionCallback,
                              String siddhiAppName, String streamID);
    protected abstract void disconnectServer(Logger logger, String siddhiAppName, String streamID);

    protected abstract SslContextBuilder getSslContextBuilder(String filePath, String password, String algorithm,
                                                              String storeType, String siddhiAppName, String streamID)
            throws KeyStoreException, NoSuchAlgorithmException, CertificateException,
            UnrecoverableKeyException;

    protected abstract SslContextBuilder addTrustStore(String filePath, String password, String algorithm,
                                            SslContextBuilder sslContextBuilder, String storeType,
                                            String siddhiAppName, String streamID)
            throws NoSuchAlgorithmException, KeyStoreException, CertificateException;


    /**
     * Pause the execution.
     */
    public void pause(Logger logger, String url) {
        lock.lock();
        try {
            paused = true;
            logger.info("Event input has paused for " + url);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Resume pool execution.
     */
    public void resume(Logger logger, String url) {
        lock.lock();
        try {
            paused = false;
            logger.info("Event input has resume for " + url);
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    protected void handlePause(Logger logger) {
        if (paused) {
            lock.lock();
            try {
                while (paused) {
                    condition.await();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Thread interrupted while pausing ", e);
            } finally {
                lock.unlock();
            }
        }
    }

}
