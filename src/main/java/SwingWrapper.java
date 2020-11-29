import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.roaringbitmap.RoaringBitmap;

import java.util.Map;
import java.util.concurrent.*;
import java.util.zip.DeflaterOutputStream;

/**
 * @author lishuangjiang
 * @date 2020/11/26
 */
public class SwingWrapper {
    private int userSize;
    private Double alpha;
    private RoaringBitmap[] userPlayMatrix;


    public SwingWrapper(int userSize, Double alpha) {
        this.userSize = userSize;
        this.alpha = alpha;
        this.userPlayMatrix = new RoaringBitmap[userSize];
    }

    public void addUserPlayList(int userId, int[] itemIds) {
        if (this.userPlayMatrix[userId] == null) {
            this.userPlayMatrix[userId] = new RoaringBitmap();
        }
        this.userPlayMatrix[userId].add(itemIds);
    }

    public void addUserPlayPair(int userId, int itemId) {
        if (this.userPlayMatrix[userId] == null) {
            this.userPlayMatrix[userId] = new RoaringBitmap();
        }
        this.userPlayMatrix[userId].add(itemId);
    }

    public Map<Tuple, Double> calcUserCrossWeight() throws InterruptedException {
        final Map<Tuple, Double> pairScore = new ConcurrentHashMap<Tuple, Double>(this.userSize * 6);
        // 使用 ThreadFactoryBuilder 创建自定义线程名称的 ThreadFactory
        int nThreads = Runtime.getRuntime().availableProcessors();
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("swing-pool-%d").build();
        // 创建线程池，其中任务队列需要结合实际情况设置合理的容量
        ThreadPoolExecutor executor = new ThreadPoolExecutor(nThreads,
                nThreads * 3,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue(1024),
                namedThreadFactory,
                new ThreadPoolExecutor.AbortPolicy());

        // 新建任务
        for (int i = 0; i < userSize - 1; ++i) {
            final int finalI = i;
            if (userPlayMatrix[finalI] == null) {
                continue;
            }
            executor.execute(new Runnable() {
                public void run() {
                    for (int j = finalI + 1; j < userSize; ++j) {
                        if (userPlayMatrix[j] == null) {
                            return;
                        }
                        RoaringBitmap andResult = RoaringBitmap.and(userPlayMatrix[finalI], userPlayMatrix[j]);
                        long crossItemSize = andResult.getLongCardinality();
                        if (crossItemSize <= 0) {
                            return;
                        }
                        double score = 1.0 / (crossItemSize + alpha);
                        for (int itemI : andResult) {
                            for (int itemJ : andResult) {
                                if (itemI > itemJ) {
                                    Tuple tuple = new Tuple(itemI, itemJ);
                                    if (pairScore.containsKey(tuple)) {
                                        Double scoreUpdate = pairScore.get(tuple) + score;
                                        pairScore.put(tuple, scoreUpdate);
                                    } else {
                                        pairScore.put(tuple, score);
                                    }
                                }
                            }
                        }
                    }
                    System.out.println("finish process user " + String.valueOf(finalI) + ";" + Thread.currentThread().getName());
                }
            });

        }
        // 优雅关闭线程池
        executor.shutdown();
        executor.awaitTermination(3600 * 10L, TimeUnit.SECONDS);
        // 任务执行完毕后打印"Done"
        System.out.println("finish calc user pair");
        return pairScore;
    }

    public int getUserSize() {
        return userSize;
    }

    public void setUserSize(int userSize) {
        this.userSize = userSize;
    }
}
