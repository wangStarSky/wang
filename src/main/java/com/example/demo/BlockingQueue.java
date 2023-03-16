package com.example.demo;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;




public class BlockingQueue<T> {

    // 队列
    private Deque<T> deque = new ArrayDeque<>();

    // 锁
    private ReentrantLock lock = new ReentrantLock();

    private Condition fullWaitSet = lock.newCondition();

    private Condition emptyWaitSet = lock.newCondition();

    // 容量
    private int capacity;

    public BlockingQueue(int capacity) {
        this.capacity = capacity;
    }

    /**
     * 阻塞式获取
     *
     * @return 线程对象
     */
    public T get() {
        lock.lock();
        try {
            while (deque.isEmpty()) {
                try {
                    emptyWaitSet.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            T t = deque.removeFirst();
            fullWaitSet.signal();
            return t;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 阻塞式添加
     *
     * @return 线程对象
     */
    public void put(T element) {
        lock.lock();
        try {
            while (capacity == deque.size()) {
                try {
                    fullWaitSet.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            deque.addLast(element);
            emptyWaitSet.signal();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 获取队列大小
     *
     * @return 队列大小
     */
    public int getSize() {
        lock.lock();
        try {
            return deque.size();
        }finally {
            lock.unlock();
        }
    }

    /**
     * 带超时时间的阻塞获取
     *
     *
     */
    public T poll(long timeout , TimeUnit unit) {
        lock.lock();
        try {
            long nanos = unit.toNanos(timeout);
            while (deque.isEmpty()) {
                try {
                    if (nanos <= 0) {
                        return null;
                    }
                    long awaitNanos = emptyWaitSet.awaitNanos(nanos);
                    nanos = awaitNanos;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            T t = deque.removeFirst();
            fullWaitSet.signal();
            return t;
        }finally {
            lock.unlock();
        }
    }
}
