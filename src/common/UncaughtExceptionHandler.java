package common;

public class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        System.out.println("Exception in thread " + t.getName() + ": ");
        e.printStackTrace();
    }
}