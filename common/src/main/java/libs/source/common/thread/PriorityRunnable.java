package libs.source.common.thread;

public class PriorityRunnable extends PriorityObject<Runnable> implements Runnable, Comparable {

    public PriorityRunnable(int priority, Runnable obj) {
        super(priority, obj);
    }

    @Override
    public void run() {
        this.obj.run();
    }

    @Override
    public int compareTo(Object o) {
        return Integer.compare(this.priority, (Integer) o);
    }
}
