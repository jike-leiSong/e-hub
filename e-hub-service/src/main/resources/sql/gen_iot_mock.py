#!/usr/bin/env python3
"""生成一天物联分钟数据写入SQL文件"""
import random
import math
from datetime import datetime, timedelta

# 配置
AGGREGATOR    = "1711340903453614182"
ENT_ID        = "1711733759670538242"
PROJECT_ID    = "PRJ001"
DEVICE1_ID    = 1
DEVICE2_ID    = 2
DEVICE1_CODE  = "METE001"
DEVICE2_CODE  = "METE002"
POINT_CODE    = "P"
UNIT          = "kW"
START_TIME    = datetime(2026, 6, 30, 0, 0, 0)   # 一天前 00:00:00
END_TIME      = datetime(2026, 6, 30, 23, 59, 0)  # 当天 23:59:00
OUT_FILE      = "iot_telemetry_minute_mock.sql"

# 测点: (基础值, 振幅, 周期分钟, 相位偏移, 噪声标准差)
POINT_DEFS = [
    ("P", UNIT, 200.0, 60.0, 1440, 0.0,   3.0),   # 功率: 日周期波动
]

def sinusoidal(base, amplitude, period_min, phase, minute_idx, noise_std):
    t = (minute_idx / period_min) * 2 * math.pi + phase
    noise = random.gauss(0, noise_std)
    return round(base + amplitude * math.sin(t) + noise, 4)

def main():
    with open(OUT_FILE, "w", encoding="utf-8") as f:
        f.write("-- iot_telemetry_minute mock data\n")
        f.write("-- aggregator_id: %s\n" % AGGREGATOR)
        f.write("-- ent_id: %s\n" % ENT_ID)
        f.write("-- device1: %s (%d)\n" % (DEVICE1_CODE, DEVICE1_ID))
        f.write("-- device2: %s (%d)\n" % (DEVICE2_CODE, DEVICE2_ID))
        f.write("-- point: %s (%s)\n" % (POINT_CODE, UNIT))
        f.write("-- 时间范围: %s ~ %s\n\n" % (START_TIME, END_TIME))
        f.write("BEGIN;\n\n")

        devices = [
            (DEVICE1_ID, DEVICE1_CODE),
            (DEVICE2_ID, DEVICE2_CODE),
        ]

        for minute_idx in range(1440):
            ts = START_TIME + timedelta(minutes=minute_idx)
            data_time   = ts.strftime("%Y-%m-%d %H:%M:%S")
            minute_time = ts.strftime("%Y-%m-%d %H:%M:00")
            receive     = (ts + timedelta(seconds=random.randint(1, 30))).strftime("%Y-%m-%d %H:%M:%S")

            for dev_id, dev_code in devices:
                for pt_code, unit, base, amp, period, phase, noise_std in POINT_DEFS:
                    val = sinusoidal(base, amp, period, phase, minute_idx, noise_std)
                    # 5%概率产生异常值
                    if random.random() < 0.05:
                        val = round(val * random.uniform(0.6, 1.4), 4)
                    quality = "abnormal" if random.random() < 0.03 else "normal"
                    raw_val = str(val)

                    sql = (
                        f"INSERT INTO iot_telemetry_minute "
                        f"(aggregator_id,ent_id,project_id,device_id,device_code,point_code,"
                        f"data_time,minute_time,point_value,unit,quality,source_code,receive_time,raw_value) "
                        f"VALUES "
                        f"('{AGGREGATOR}','{ENT_ID}','{PROJECT_ID}',{dev_id},'{dev_code}','{pt_code}',"
                        f"'{data_time}','{minute_time}',{val},'{unit}','{quality}','SRC001','{receive}','{raw_val}');\n"
                    )
                    f.write(sql)

        f.write("\nCOMMIT;\n")

    rows = len(devices) * len(POINT_DEFS) * 1440
    size_kb = round(len(open(OUT_FILE).read()) / 1024, 1)
    print(f"生成完毕: {OUT_FILE}")
    print(f"  总行数: {rows:,} 条")
    print(f"  文件大小: ~{size_kb} KB")

if __name__ == "__main__":
    main()
