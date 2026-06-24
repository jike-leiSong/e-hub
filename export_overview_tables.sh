#!/bin/bash

# E-HUB 负荷聚合平台 - 运营总览模块数据库表导出脚本
# 用途：从原项目数据库导出运营总览页面所需的所有表结构

# 数据库连接信息
DB_HOST="10.39.41.241"
DB_PORT="3306"
DB_NAME="load-aggregator"
DB_USER="load_admin_r"
DB_PASS="pAmBiCabP4Qz7ojG50RrcZqX"

# 输出目录
OUTPUT_DIR="./sql/overview_tables"
mkdir -p $OUTPUT_DIR

echo "======================================"
echo "E-HUB 运营总览模块 - 数据库表导出"
echo "======================================"
echo ""
echo "数据库: $DB_NAME@$DB_HOST:$DB_PORT"
echo "输出目录: $OUTPUT_DIR"
echo ""

# P0核心表（运营总览必需）
echo ">>> 正在导出 P0 核心表（运营总览必需）..."
TABLES_P0=(
    "aggregator_info"
    "aggregator_ent"
    "aggregator_ent_device"
    "aggregator_date_profit"
    "aggregator_ent_date_profit"
    "aggregator_apply_plan"
    "aggregator_date_apply_detail"
    "aggregator_date_apply_detail_offer"
    "aggregator_resource_type"
)

for table in "${TABLES_P0[@]}"; do
    echo "  - 导出: $table"
    mysqldump -h $DB_HOST -P $DB_PORT -u $DB_USER -p$DB_PASS \
        --no-data \
        --skip-add-drop-table \
        --skip-comments \
        $DB_NAME $table > "$OUTPUT_DIR/${table}.sql" 2>/dev/null

    if [ $? -eq 0 ]; then
        echo "    ✅ 成功"
    else
        echo "    ❌ 失败"
    fi
done

echo ""
echo ">>> 正在导出 P1 重要功能表..."
TABLES_P1=(
    "aggregator_ent_apply_plan"
    "aggregator_device_date_profit"
    "aggregator_device_date_base_line_load_chart"
    "aggregator_device_date_delivery_chart"
    "aggregator_device_date_issue_chart"
    "aggregator_ent_date_invite_detail"
    "aggregator_ent_device_iot_log"
)

for table in "${TABLES_P1[@]}"; do
    echo "  - 导出: $table"
    mysqldump -h $DB_HOST -P $DB_PORT -u $DB_USER -p$DB_PASS \
        --no-data \
        --skip-add-drop-table \
        --skip-comments \
        $DB_NAME $table > "$OUTPUT_DIR/${table}.sql" 2>/dev/null

    if [ $? -eq 0 ]; then
        echo "    ✅ 成功"
    else
        echo "    ❌ 失败"
    fi
done

echo ""
echo ">>> 正在导出 P2 辅助功能表..."
TABLES_P2=(
    "aggregator_date_holiday"
    "aggregator_ent_app_apply_plan"
    "aggregator_ent_date_apply_detail"
)

for table in "${TABLES_P2[@]}"; do
    echo "  - 导出: $table"
    mysqldump -h $DB_HOST -P $DB_PORT -u $DB_USER -p$DB_PASS \
        --no-data \
        --skip-add-drop-table \
        --skip-comments \
        $DB_NAME $table > "$OUTPUT_DIR/${table}.sql" 2>/dev/null

    if [ $? -eq 0 ]; then
        echo "    ✅ 成功"
    else
        echo "    ❌ 失败"
    fi
done

echo ""
echo ">>> 合并SQL文件..."

# 合并P0核心表
echo "  - 合并P0核心表 -> 00_overview_core_tables.sql"
cat > "$OUTPUT_DIR/00_overview_core_tables.sql" << 'EOF'
-- ====================================
-- E-HUB 运营总览模块 - P0核心表
-- 创建日期: 2026-06-23
-- 说明: 运营总览页面必需的核心数据库表
-- ====================================

USE e_hub;

EOF

for table in "${TABLES_P0[@]}"; do
    if [ -f "$OUTPUT_DIR/${table}.sql" ]; then
        echo "" >> "$OUTPUT_DIR/00_overview_core_tables.sql"
        echo "-- Table: $table" >> "$OUTPUT_DIR/00_overview_core_tables.sql"
        cat "$OUTPUT_DIR/${table}.sql" >> "$OUTPUT_DIR/00_overview_core_tables.sql"
        echo "" >> "$OUTPUT_DIR/00_overview_core_tables.sql"
    fi
done

# 合并P1重要表
echo "  - 合并P1重要表 -> 01_overview_important_tables.sql"
cat > "$OUTPUT_DIR/01_overview_important_tables.sql" << 'EOF'
-- ====================================
-- E-HUB 运营总览模块 - P1重要表
-- 创建日期: 2026-06-23
-- 说明: 运营总览页面重要功能的数据库表
-- ====================================

USE e_hub;

EOF

for table in "${TABLES_P1[@]}"; do
    if [ -f "$OUTPUT_DIR/${table}.sql" ]; then
        echo "" >> "$OUTPUT_DIR/01_overview_important_tables.sql"
        echo "-- Table: $table" >> "$OUTPUT_DIR/01_overview_important_tables.sql"
        cat "$OUTPUT_DIR/${table}.sql" >> "$OUTPUT_DIR/01_overview_important_tables.sql"
        echo "" >> "$OUTPUT_DIR/01_overview_important_tables.sql"
    fi
done

# 合并P2辅助表
echo "  - 合并P2辅助表 -> 02_overview_auxiliary_tables.sql"
cat > "$OUTPUT_DIR/02_overview_auxiliary_tables.sql" << 'EOF'
-- ====================================
-- E-HUB 运营总览模块 - P2辅助表
-- 创建日期: 2026-06-23
-- 说明: 运营总览页面辅助功能的数据库表
-- ====================================

USE e_hub;

EOF

for table in "${TABLES_P2[@]}"; do
    if [ -f "$OUTPUT_DIR/${table}.sql" ]; then
        echo "" >> "$OUTPUT_DIR/02_overview_auxiliary_tables.sql"
        echo "-- Table: $table" >> "$OUTPUT_DIR/02_overview_auxiliary_tables.sql"
        cat "$OUTPUT_DIR/${table}.sql" >> "$OUTPUT_DIR/02_overview_auxiliary_tables.sql"
        echo "" >> "$OUTPUT_DIR/02_overview_auxiliary_tables.sql"
    fi
done

echo ""
echo "======================================"
echo "✅ 导出完成！"
echo "======================================"
echo ""
echo "导出文件："
echo "  - 单表SQL: $OUTPUT_DIR/*.sql"
echo "  - P0核心表: $OUTPUT_DIR/00_overview_core_tables.sql"
echo "  - P1重要表: $OUTPUT_DIR/01_overview_important_tables.sql"
echo "  - P2辅助表: $OUTPUT_DIR/02_overview_auxiliary_tables.sql"
echo ""
echo "下一步："
echo "  1. 检查合并后的SQL文件"
echo "  2. 根据需要调整字符集和引擎"
echo "  3. 在新项目数据库中执行SQL"
echo ""
echo "执行命令："
echo "  mysql -h <host> -u <user> -p e_hub < $OUTPUT_DIR/00_overview_core_tables.sql"
echo ""
