package inf5171.monitor;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by david on 2016-12-11.
 */
public class MStructureMonitor<T> implements MStructure<T> {
    private final Queue<T> queue;
    private final ReentrantLock lock;
    private final Condition notEmpty;
    private volatile Boolean completed;

    public MStructureMonitor(){
        queue = new ArrayDeque<T>();
        lock = new ReentrantLock();
        notEmpty = lock.newCondition();
        completed = false;
    }

    public Queue<T> getQueue() {
        return queue;
    }

    @Override
    public Boolean push(T v) {
        lock.lock();
        Boolean added = false;
        try {

            added = queue.add(v);
            if (queue.add(v)) {
                notEmpty.signalAll();
            }
        } finally {
            lock.unlock();
        }

        return added;
    }

    @Override
    public Boolean push(List<T> list) {
        lock.lock();
        Boolean added = false;
        try {
            added = queue.addAll(list);
            if (added) {
                notEmpty.signalAll();
            }
        } finally {
            lock.unlock();
        }

        return added;
    }

    @Override
    public T shift() {
        lock.lock();
        T value = null;
        try {
            while (queue.size() == 0 && ! completed) {
//                System.out.println("AAA");
                try {
                    notEmpty.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            value = queue.poll();

        } finally {
            lock.unlock();
        }

        return value;
    }

    @Override
    public synchronized int size() {
        return queue.size();
    }

    @Override
    public synchronized void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    @Override
    public Boolean getCompleted() {
        return completed;
    }
}
