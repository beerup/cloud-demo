package cn.itcast.order.util;

/**
 * @author tsh
 * @version 1.0
 * @date 2022/4/21 21:35
 */
public class SnowFlake {

    // ==============================Fields===========================================
    /**
     * 开始时间截 (2019-01-01)
     */
//    private final static long twepoch = 1650904740185L;
    private final static long twepoch = 1650904740L;

    /**
     * 机器id所占的位数
     */
    private final static long workerIdBits = 6L;

    /**
     * 支持的最大机器id，结果是64 (这个移位算法可以很快的计算出几位二进制数所能表示的最大十进制数)
     */
    private final static long maxWorkerId = ~(-1L << workerIdBits);

    /**
     * 序列在id中占的位数
     */
    private final static long sequenceBits = 12L;

    /**
     * 机器ID向左移6位
     */
    private final static long workerIdShift = sequenceBits;

    /**
     * 时间截向左移12位(6+6)
     */
    private final static long timestampLeftShift = sequenceBits + workerIdBits;

    /**
     * 生成序列的掩码，这里为4095 (0b111111111111=0xfff=4095)
     */
    private final static long sequenceMask = ~(-1L << sequenceBits);

    /**
     * 工作机器ID(0~64)
     */
    private final long workerId;

    /**
     * 毫秒内序列(0~4095)
     */
    private long sequence = 0L;

    /**
     * 上次生成ID的时间截
     */
    private long lastTimestamp = -1L;

    /**
     * 默认实现，工作ID用本机ip生成，数据中心ID 用随机数
     */
    private static final SnowFlake SnowFlake;


    //==============================Constructors=====================================

    /**
     * 构造函数
     *
     * @param workerId 工作ID (0~31)
     */
    private SnowFlake(long workerId) {
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
        }
        this.workerId = workerId;
    }

    // ==============================Methods==========================================

    /**
     * 获得下一个ID (该方法是线程安全的)
     *
     * @return SnowflakeId
     */
    public synchronized long nextId() {
        long timestamp = timeGen();

        //如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过这个时候应当抛出异常
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(
                    String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
        }

        //如果是同一时间生成的，则进行毫秒内序列
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            //毫秒内序列溢出
            if (sequence == 0) {
                //阻塞到下一个毫秒,获得新的时间戳
                timestamp = tilNextMillis(lastTimestamp);
            }
        }
        //时间戳改变，毫秒内序列重置
        else {
            //sequence = 0L;
            sequence = timestamp & 1L; //根据当前时间戳单or双数重置序列，（修正在大多数情况下生成偶数的情况）
        }

        //上次生成ID的时间截
        lastTimestamp = timestamp;

        //移位并通过或运算拼到一起组成64位的ID
        return ((timestamp - twepoch) << timestampLeftShift) | (workerId << workerIdShift) | sequence;
    }

    /**
     * 阻塞到下一个毫秒，直到获得新的时间戳
     *
     * @param lastTimestamp 上次生成ID的时间截
     * @return 当前时间戳
     */
    protected long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    /**
     * 返回以毫秒为单位的当前时间
     *
     * @return 当前时间(毫秒)
     */
    protected long timeGen() {
        return System.currentTimeMillis() / 1000;
    }


    // public static void main(String[] args) {
        // System.out.println(newId());
    // }


    /*默认实现 逻辑：
    工作id -> 用服务器ip生成，ip转化为long再模32（如有集群部署，且机器ip是连续的,
    如我的集群服务器是192.168.180.54~58共五台机器，这样取模就能避免重复.如服务器ip不连续，慎用，慎用，慎用！！！！！！ 重要事情说三遍）
    数据中心ID -> 取0～31的随机数*/
    static {
        SnowFlake = new SnowFlake(63);
    }

    //默认实现
    public static long newId() {
        return SnowFlake.nextId();
    }

}
