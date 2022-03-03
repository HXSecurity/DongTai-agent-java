package io.dongtai.iast.common.entity.performance.metrics;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 垃圾回收信息指标
 *
 * @author chenyi
 * @date 2022/3/1
 * @see java.lang.management.GarbageCollectorMXBean
 */
public class GarbageInfoMetrics implements Serializable {
    private static final long serialVersionUID = -224612690928046790L;

    /**
     * 有效的内存管理器列表
     */
    private final List<CollectionInfo> collectionInfoList = new ArrayList<CollectionInfo>();


    public void addCollectionInfo(String collectionName, Long collectionCount, Long collectionTime) {
        final CollectionInfo collectionInfo = new CollectionInfo();
        collectionInfo.setCollectionName(collectionName);
        collectionInfo.setCollectionCount(collectionCount);
        collectionInfo.setCollectionTime(collectionTime);
        this.collectionInfoList.add(collectionInfo);
    }

    public List<CollectionInfo> getCollectionInfoList() {
        return collectionInfoList;
    }

    @Override
    public String toString() {
        return collectionInfoList.toString();
    }

    /**
     * 垃圾收集信息
     */
    public static class CollectionInfo implements Serializable {
        private static final long serialVersionUID = -6668180967516170799L;

        /**
         * 收集器名称
         */
        public String collectionName;
        /**
         * 收集次数
         */
        public long collectionCount;

        /**
         * 收集时间
         */
        public long collectionTime;

        public long getCollectionCount() {
            return collectionCount;
        }

        public void setCollectionCount(long collectionCount) {
            this.collectionCount = collectionCount;
        }

        public long getCollectionTime() {
            return collectionTime;
        }

        public void setCollectionTime(long collectionTime) {
            this.collectionTime = collectionTime;
        }

        public String getCollectionName() {
            return collectionName;
        }

        public void setCollectionName(String collectionName) {
            this.collectionName = collectionName;
        }

        @Override
        public String toString() {
            return "collectionName = " + collectionName +
                    " collectionCount = " + collectionCount +
                    " collectionTime = " + collectionTime;
        }
    }
}
