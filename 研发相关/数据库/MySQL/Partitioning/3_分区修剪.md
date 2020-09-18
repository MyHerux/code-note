## 分区修剪

分区修剪背后的核心概念相对简单，可以描述为“不扫描没有匹配值的分区”。假设您有一个由以下语句定义的分区 `table t1`：
```
CREATE TABLE t1 (
    fname VARCHAR(50) NOT NULL,
    lname VARCHAR(50) NOT NULL,
    region_code TINYINT UNSIGNED NOT NULL,
    dob DATE NOT NULL
)
PARTITION BY RANGE( region_code ) (
    PARTITION p0 VALUES LESS THAN (64),
    PARTITION p1 VALUES LESS THAN (128),
    PARTITION p2 VALUES LESS THAN (192),
    PARTITION p3 VALUES LESS THAN MAXVALUE
);
```

您希望从诸如此类的SELECT语句获得结果的情况：
```
SELECT fname, lname, region_code, dob
    FROM t1
    WHERE region_code > 125 AND region_code < 130;
```

显而易见，应该返回的行都不会在分区 `p0` 或 `p3` 中；也就是说，我们只需要在分区 `p1` 和 `p2` 中搜索以找到匹配的行。这样，与扫描 `table` 中的所有分区相比，在查找匹配行上可以花费更少的时间和精力。这种“切掉”不需要的分区称为修剪。当优化器可以在执行此查询时使用分区修剪时，对于包含相同列定义和数据的未分区 `table` ，查询的执行可能比同一查询快一个数量级。

只要将 `WHERE` 条件减少到以下两种情况之一，优化器就可以执行修剪：

- `partition_column = constant`

- `partition_column IN (constant1, constant2, ..., constantN)`

在第一种情况下，优化器仅对给定值的分区 `table` 达式求值，确定哪个分区包含该值，然后仅扫描该分区。在许多情况下，等号可用另一种算术比较代替，包括 `<`，`>`，`<=`，`>=` 和 `<>`。在 `WHERE` 子句中使用 `BETWEEN` 的某些查询也可以利用分区修剪功能。

在第二种情况下，优化器为列 `table` 中的每个值评估分区 `table` 达式，创建匹配分区的列 `table` ，然后仅扫描此分区列 `table` 中的分区。

`MySQL` 可以将分区修剪应用于 `SELECT` ， `DELETE` 和 `UPDATE` 语句。 `INSERT` 语句还只对每个插入的行访问一个分区；即使对于由 `HASH` 或 `KEY` 分区的 `table` 也是如此。

修剪还可以应用于短范围，优化器可以将其转换为等效的值列 `table` 。例如，在前面的示例中， `WHERE` 子句可以转换为 `WHERE region_code IN (126, 127, 128, 129)`。然后，优化器可以确定列 `table` 中的前两个值是在分区 `p1` 中找到的，其余两个值是在分区 `p2` 中的，并且其他分区不包含任何相关值，因此不需要搜索匹配行。

优化程序还可以针对 `WHERE` 条件执行修剪，这些条件涉及使用 `RANGECOLUMNS` 或 `LISTCOLUMNS` 分区的 `table` 在多列上的先前类型的比较。

只要分区 `table` 达式包含一个等于或可减少为一组相等的范围的范围，或者当分区 `table` 达式 `table` 示增加或减少的关系时，都可以应用这种类型的优化。当分区 `table` 达式使用 `YEAR()` 或 `TO_DAYS()` 函数时，修剪还可以应用于在 `DATE` 或 `DATETIME` 列上分区的 `table` 。

假设按如下所示定义的 `table t2` 被分区在 `DATE` 列上：
```
CREATE TABLE t2 (
    fname VARCHAR(50) NOT NULL,
    lname VARCHAR(50) NOT NULL,
    region_code TINYINT UNSIGNED NOT NULL,
    dob DATE NOT NULL
)
PARTITION BY RANGE( YEAR(dob) ) (
    PARTITION d0 VALUES LESS THAN (1970),
    PARTITION d1 VALUES LESS THAN (1975),
    PARTITION d2 VALUES LESS THAN (1980),
    PARTITION d3 VALUES LESS THAN (1985),
    PARTITION d4 VALUES LESS THAN (1990),
    PARTITION d5 VALUES LESS THAN (2000),
    PARTITION d6 VALUES LESS THAN (2005),
    PARTITION d7 VALUES LESS THAN MAXVALUE
);
```

以下使用t2的语句可以使用分区修剪：
```
SELECT * FROM t2 WHERE dob = '1982-06-23';

UPDATE t2 SET region_code = 8 WHERE dob BETWEEN '1991-02-15' AND '1997-04-25';

DELETE FROM t2 WHERE dob >= '1984-06-21' AND dob <= '1999-06-21'
```

对于最后一条语句，优化器还可以执行以下操作：

- 找到包含范围下限的分区。

   `YEAR('1984-06-21')` 产生值 `1984` ，该值在分区d3中找到。

- 找到包含范围高端的分区。

    `YEAR('1999-06-21')` 的计算结果为 `1999` ，位于分区d5中。

- 仅扫描这两个分区以及它们之间可能存在的任何分区（`d3` , `d4` , `d5`）。

修剪也可以应用于其他分区类型。

考虑一个被 `LIST` 分区的 `table` ，其中分区 `table` 达式在增加或减少，例如此处显示的 `tablet3` 。(在本示例中，为简洁起见，我们假设 `region_code` 列的值限制为 `1` 到 `10` 之间(包括 `1` 和 `10` )。
```
CREATE TABLE t3 (
    fname VARCHAR(50) NOT NULL,
    lname VARCHAR(50) NOT NULL,
    region_code TINYINT UNSIGNED NOT NULL,
    dob DATE NOT NULL
)
PARTITION BY LIST(region_code) (
    PARTITION r0 VALUES IN (1, 3),
    PARTITION r1 VALUES IN (2, 5, 8),
    PARTITION r2 VALUES IN (4, 9),
    PARTITION r3 VALUES IN (6, 7, 10)
);
```

对于诸如 `SELECT * FROM t3 WHERE region_code BETWEEN 1 AND 3` 之类的语句，优化器确定在哪个分区中找到值 `1、2` 和 `3` ( `r0` 和 `r1` )，并跳过其余的值( `r2` 和 `r3` )。

对于被 `HASH` 或`[LINEAR] KEY` 分区的 `table` ，如果 `WHERE` 子句对分区 `table` 达式中使用的列使用简单的=关系，则也可以进行分区修剪。考虑这样创建的 `table`：
```
CREATE TABLE t4 (
    fname VARCHAR(50) NOT NULL,
    lname VARCHAR(50) NOT NULL,
    region_code TINYINT UNSIGNED NOT NULL,
    dob DATE NOT NULL
)
PARTITION BY KEY(region_code)
PARTITIONS 8;
```

可以删除将列值与常量进行比较的语句：
```
UPDATE t4 WHERE region_code = 7;
```

修剪还可以用于短区间，因为优化程序可以将这种条件转换为 `IN` 关系。例如，使用与先前定义的 `table t4` 相同的查询可以被修剪：
```
SELECT * FROM t4 WHERE region_code > 2 AND region_code < 6;

SELECT * FROM t4 WHERE region_code BETWEEN 3 AND 5;
```

在这两种情况下，优化器都会将 `WHERE` 子句转换为 `WHERE region_code IN (3, 4, 5)`。