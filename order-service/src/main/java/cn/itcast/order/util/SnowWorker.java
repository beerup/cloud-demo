package cn.itcast.order.util;

/**
 * @author tsh
 * @version 1.0
 * @date 2022/4/24 20:48
 */
public class SnowWorker {

    /**
     * 基础时间（ms单位）
     * 不能超过当前系统时间
     */
    private final long BaseTime = 1582136402000L;

    /**
     * 机器码
     * 必须由外部设定，最大值 2^WorkerIdBitLength-1
     */
    private final int WorkerId;

    /**
     * 机器码位长
     * 默认值6，取值范围 [1, 15]（要求：序列数位长+机器码位长不超过22）
     */
    private final int WorkerIdBitLength = 6;

    /**
     * 序列数位长
     * 默认值6，取值范围 [3, 21]（要求：序列数位长+机器码位长不超过22）
     */
    private final int SeqBitLength = 12;

    /**
     * 最大序列数（含）
     * 设置范围 [MinSeqNumber, 2^SeqBitLength-1]，默认值0，表示最大序列数取最大值（2^SeqBitLength-1]）
     */
    private final int MaxSeqNumber = (1 << SeqBitLength) - 1;

    /**
     * 最小序列数（含）
     * 默认值5，取值范围 [5, MaxSeqNumber]，每毫秒的前5个序列数对应编号是0-4是保留位，其中1-4是时间回拨相应预留位，0是手工新值预留位
     */
    public int MinSeqNumber = 5;

    /**
     * 最大漂移次数（含）
     * 默认2000，推荐范围500-10000（与计算能力有关）
     */
    public int TopOverCostCount = 2000;

    private final byte _TimestampShift;

    private int _CurrentSeqNumber;
    private long _LastTimeTick = 0;
    private long _TurnBackTimeTick = 0;
    private byte _TurnBackIndex = 0;

    private boolean _IsOverCost = false;
    private int _OverCostCountInOneTerm = 0;
    private int _TermIndex = 0;

    public static SnowWorker snowWorker;

    private SnowWorker(int workerId) {
        this.WorkerId = workerId;
        _TimestampShift = (byte) (WorkerIdBitLength + SeqBitLength);
        _CurrentSeqNumber = MinSeqNumber;
    }


    private void EndOverCostAction() {
        if (_TermIndex > 10000) {
            _TermIndex = 0;
        }
    }

    private long NextOverCostId() {
        long currentTimeTick = GetCurrentTimeTick();

        if (currentTimeTick > _LastTimeTick) {
            EndOverCostAction();

            _LastTimeTick = currentTimeTick;
            _CurrentSeqNumber = MinSeqNumber;
            _IsOverCost = false;
            _OverCostCountInOneTerm = 0;

            return CalcId(_LastTimeTick);
        }

        if (_OverCostCountInOneTerm >= TopOverCostCount) {
            EndOverCostAction();

            _LastTimeTick = GetNextTimeTick();
            _CurrentSeqNumber = MinSeqNumber;
            _IsOverCost = false;
            _OverCostCountInOneTerm = 0;

            return CalcId(_LastTimeTick);
        }

        if (_CurrentSeqNumber > MaxSeqNumber) {
            _LastTimeTick++;
            _CurrentSeqNumber = MinSeqNumber;
            _IsOverCost = true;
            _OverCostCountInOneTerm++;

            return CalcId(_LastTimeTick);
        }

        return CalcId(_LastTimeTick);
    }

    private long NextNormalId() {
        long currentTimeTick = GetCurrentTimeTick();

        if (currentTimeTick < _LastTimeTick) {
            if (_TurnBackTimeTick < 1) {
                _TurnBackTimeTick = _LastTimeTick - 1;
                _TurnBackIndex++;

                // 每毫秒序列数的前5位是预留位，0用于手工新值，1-4是时间回拨次序
                // 支持4次回拨次序（避免回拨重叠导致ID重复），可无限次回拨（次序循环使用）。
                if (_TurnBackIndex > 4) {
                    _TurnBackIndex = 1;
                }
            }

            return CalcTurnBackId(_TurnBackTimeTick);
        }

        // 时间追平时，_TurnBackTimeTick清零
        if (_TurnBackTimeTick > 0) {
            _TurnBackTimeTick = 0;
        }

        if (currentTimeTick > _LastTimeTick) {
            _LastTimeTick = currentTimeTick;
            _CurrentSeqNumber = MinSeqNumber;

            return CalcId(_LastTimeTick);
        }

        if (_CurrentSeqNumber > MaxSeqNumber) {

            _TermIndex++;
            _LastTimeTick++;
            _CurrentSeqNumber = MinSeqNumber;
            _IsOverCost = true;
            _OverCostCountInOneTerm = 1;

            return CalcId(_LastTimeTick);
        }

        return CalcId(_LastTimeTick);
    }

    private long CalcId(long useTimeTick) {
        long result = ((useTimeTick << _TimestampShift) +
                ((long) WorkerId << SeqBitLength) +
                _CurrentSeqNumber);

        _CurrentSeqNumber++;
        return result;
    }

    private long CalcTurnBackId(long useTimeTick) {
        long result = ((useTimeTick << _TimestampShift) +
                ((long) WorkerId << SeqBitLength) + _TurnBackIndex);

        _TurnBackTimeTick--;
        return result;
    }

    private long GetCurrentTimeTick() {
        long millis = System.currentTimeMillis();
        return millis - BaseTime;
    }

    private long GetNextTimeTick() {
        long tempTimeTicker = GetCurrentTimeTick();

        while (tempTimeTicker <= _LastTimeTick) {
            tempTimeTicker = GetCurrentTimeTick();
        }

        return tempTimeTicker;
    }

    /**
     *
     * 899.2805755395683
     * 4,424.778761061947
     *
     *
     * 129053495681099        (运行1年)
     * 387750301904971        (运行3年)
     * 646093214093387        (运行5年)
     * 1292658282840139       (运行10年)
     * 9007199254740992       (js Number 最大值，可以支撑70年)
     * 165399880288699493     (普通雪花算法生成的ID)
     * @return
     */

    public synchronized long nextId() {
        return _IsOverCost ? NextOverCostId() : NextNormalId();
    }

    static {
        snowWorker = new SnowWorker(1);
    }

    public static long newId() {
        return snowWorker.nextId();
    }

    public static void main(String[] args) {
        System.out.println(newId());
    }


}
