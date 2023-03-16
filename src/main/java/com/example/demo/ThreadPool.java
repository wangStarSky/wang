package com.example.demo;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

public class ThreadPool {
    private BlockingQueue<Runnable> taskQue;

    private HashSet<Worker> works = new HashSet<>();

    // 核心线程数
    private int coreSize;

    // 超时时间
    private long timeout;

    // 时间单位
    private TimeUnit timeUnit;

    public ThreadPool(int coreSize, long timeout, TimeUnit timeUnit, int capacity) {
        this.coreSize = coreSize;
        this.timeout = timeout;
        this.timeUnit = timeUnit;
        this.taskQue = new BlockingQueue<>(capacity);
    }

    // 提交任务
    public void execute(Runnable t) {

        // 如果现有任务数超过了coreSize,加入任务队列
        // 如果没有超过coreSize，则交给Worker对象执行
        synchronized (works) {
            if (works.size() < coreSize) {
                Worker worker = new Worker(t);
                works.add(worker);
                worker.start();
            }else {
                taskQue.put(t);
            }
        }

    }

    class Worker extends Thread {

        private  Runnable task;

        public Worker(Runnable t) {
            this.task = t;
        }

        @Override
        public void run() {
            // 当任务不为空，执行任务
            // 当当前任务执行完后，从阻塞队列里面取任务
            while (task != null || (task = taskQue.get()) != null) {
                try {
                    task.run();
                }catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    task = null;
                }
            }

            synchronized (works) {
                works.remove(this);
            }

        }
    }
}
