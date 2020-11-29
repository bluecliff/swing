import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.roaringbitmap.RoaringBitmap;

import java.util.Map;
import java.util.concurrent.*;

/**
 * @author lishuangjiang
 * @date 2020/11/26
 */
public class ItemPair {
    private int itemSize;
    private RoaringBitmap[] itemPlayMatrix;

    public ItemPair(int itemSize) {
        this.itemSize = itemSize;
    }

    public void addItemPlayPair(int itemId, int userId) {
        this.itemPlayMatrix[itemId].add(userId);
    }

    public void calcSwing(final Map<Tuple, Float> userCrossWeight) throws InterruptedException {
        int nThreads = Runtime.getRuntime().availableProcessors();
        final Map<Tuple, Float> itemCross = new ConcurrentHashMap<Tuple, Float>(this.itemSize * 6);
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("swing-pool-%d").build();
        ThreadPoolExecutor executor = new ThreadPoolExecutor(nThreads,
                nThreads * 3,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue(1024),
                namedThreadFactory,
                new ThreadPoolExecutor.AbortPolicy());
        for (int i = 0; i < itemSize - 1; ++i) {
            final int finalI = i;
            executor.execute(new Runnable() {
                public void run() {
                    for (int j = finalI + 1; j < itemSize; ++j) {
                        RoaringBitmap andResult = RoaringBitmap.and(itemPlayMatrix[finalI], itemPlayMatrix[j]);
                        Float score = 0.0f;
                        for (int u : andResult) {
                            for (int v : andResult) {
                                if (v > u) {
                                    Tuple tuple = new Tuple(u, v);
                                    Float w = userCrossWeight.get(tuple);
                                    if (w != null) {
                                        score += w;
                                    }
                                }
                            }
                        }
                        if (score > 0) {

                        }
                    }
                }
            });
        }
        // 优雅关闭线程池
        executor.shutdown();
        executor.awaitTermination(3600 * 10L, TimeUnit.SECONDS);
        // 任务执行完毕后打印"Done"
        System.out.println("finish calc user pair");
    }
}
