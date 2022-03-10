package io.dongtai.iast.common.entity.performance.metrics;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * 获得匹配名称的收集器信息
     *
     * @param collectionName 收集器名称
     * @return {@link CollectionInfo}
     */
    public CollectionInfo getMatchedFirst(String collectionName) {
        if (collectionName != null) {
            for (CollectionInfo each : getCollectionInfoList()) {
                if (collectionName.equals(each.getCollectionName())) {
                    return each;
                }
            }
        }
        return null;
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
        public Long collectionCount;
        /**
         * 收集时间
         */
        public Long collectionTime;
        /**
         * 是否是老年代收集器
         */
        public Boolean tenured;

        public Long getCollectionCount() {
            return collectionCount;
        }

        public void setCollectionCount(Long collectionCount) {
            this.collectionCount = collectionCount;
        }

        public Long getCollectionTime() {
            return collectionTime;
        }

        public void setCollectionTime(Long collectionTime) {
            this.collectionTime = collectionTime;
        }

        public String getCollectionName() {
            return collectionName;
        }

        public void setCollectionName(String collectionName) {
            this.collectionName = collectionName;
            // 同时判断收集器年代
            final GcGenerationAgeEnum gcGenerationAgeEnum = GcGenerationAgeEnum.fromGcName(collectionName);
            this.tenured = gcGenerationAgeEnum == GcGenerationAgeEnum.OLD;
        }

        public boolean isTenured() {
            return tenured;
        }

        public void setTenured(boolean tenured) {
            this.tenured = tenured;
        }
    }

    /**
     * 垃圾回收器GC年龄代枚举
     *
     * @author chenyi
     * @date 2022/03/06
     */
    public enum GcGenerationAgeEnum {
        /**
         * 老年代
         */
        OLD,
        /**
         * 年轻代
         */
        YOUNG,
        /**
         * 未知
         */
        UNKNOWN;

        private static final Map<String, GcGenerationAgeEnum> KNOWN_COLLECTORS = new HashMap<String, GcGenerationAgeEnum>() {
            private static final long serialVersionUID = -7562565756559810887L;

            {
                // Serial收集器
                put("MarkSweepCompact", OLD);
                put("Copy", YOUNG);
                // CMS收集器
                put("ConcurrentMarkSweep", OLD);
                put("ParNew", YOUNG);
                // G1收集器
                put("G1 Old Generation", OLD);
                put("G1 Young Generation", YOUNG);
                // Parallel收集器
                put("PS MarkSweep", OLD);
                put("PS Scavenge", YOUNG);
                // IBM OpenJ9收集器
                put("global", OLD);
                put("scavenge", YOUNG);

                put("partial gc", YOUNG);
                put("global garbage collect", OLD);
                // No-Op(JDK11+)
                put("Epsilon", OLD);
            }
        };

        public static GcGenerationAgeEnum fromGcName(String gcName) {
            if (gcName == null || !KNOWN_COLLECTORS.containsKey(gcName)) {
                return UNKNOWN;
            }
            return KNOWN_COLLECTORS.get(gcName);
        }
    }
}
