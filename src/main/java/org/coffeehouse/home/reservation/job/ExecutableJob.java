package org.coffeehouse.home.reservation.job;

import java.util.concurrent.Callable;

public interface ExecutableJob<V> extends Callable<V>{
}
