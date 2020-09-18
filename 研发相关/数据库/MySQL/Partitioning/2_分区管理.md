## 分区管理

`MySQL5.7` 提供了多种修改分区 `table` 的方法。可以添加，删除，重新定义，合并或拆分现有分区。所有这些动作都可以使用 `ALTERTABLE` 语句的分区扩展来执行。

### RANGE 和 LIST 分区的 Management

`RANGE` 分区和 `LIST` 分区的添加和删除类似。

可以使用带有 `DROP PARTITION` 选项的 `ALTERTABLE` 语句从由 `RANGE` 或 `LIST` 分区的 `table` 中删除分区。假设您创建了一个按范围划分的 `table` ，然后使用以下 `CREATETABLE` 和 `INSERT` 语句填充了 `10` 条记录：

```
mysql> CREATE TABLE tr (id INT, name VARCHAR(50), purchased DATE)
         PARTITION BY RANGE( YEAR(purchased) ) (
             PARTITION p0 VALUES LESS THAN (1990),
             PARTITION p1 VALUES LESS THAN (1995),
             PARTITION p2 VALUES LESS THAN (2000),
             PARTITION p3 VALUES LESS THAN (2005),
             PARTITION p4 VALUES LESS THAN (2010),
             PARTITION p5 VALUES LESS THAN (2015)
         );
Query OK, 0 rows affected (0.28 sec)

mysql> INSERT INTO tr VALUES
         (1, 'desk organiser', '2003-10-15'),
         (2, 'alarm clock', '1997-11-05'),
         (3, 'chair', '2009-03-10'),
         (4, 'bookcase', '1989-01-10'),
         (5, 'exercise bike', '2014-05-09'),
         (6, 'sofa', '1987-06-05'),
         (7, 'espresso maker', '2011-11-22'),
         (8, 'aquarium', '1992-08-04'),
         (9, 'study desk', '2006-09-16'),
         (10, 'lava lamp', '1998-12-25');
Query OK, 10 rows affected (0.05 sec)
Records: 10  Duplicates: 0  Warnings: 0
```

**查看分区 `p2` 的数据：**

```
mysql> SELECT * FROM tr PARTITION (p2);
+------+-------------+------------+
| id   | name        | purchased  |
+------+-------------+------------+
|    2 | alarm clock | 1997-11-05 |
|   10 | lava lamp   | 1998-12-25 |
+------+-------------+------------+
2 rows in set (0.00 sec)
```

**删除分区 `p2`：**

```
mysql> ALTER TABLE tr DROP PARTITION p2;
Query OK, 0 rows affected (0.03 sec)
```

> 注意：删除分区时，您还将删除存储在该分区中的所有数据！

**如果希望在保留 `table` 定义及其分区方案的同时删除所有分区中的所有数据：**

```
ALTER TABLE tr TRUNCATE PARTITION p2;
```

**如果要更改 `table` 的分区而又不丢失数据：**

可以重新划分为多个分区：

```
ALTER TABLE tr REORGANIZE PARTITION p2 INTO (PARTITION p21 VALUES LESS THAN (1998),PARTITION p22 VALUES LESS THAN (2000)) ;
```
也可以合并分区：
```
ALTER TABLE tr REORGANIZE PARTITION p21,p22 INTO (PARTITION p22 VALUES LESS THAN (2000));
```

**添加分区：**

对于比当前分区更高的分区，可以直接添加。对于比当前分区更低的分区添加，可以参考使用 `REORGANIZE`：

```
ALTER TABLE tr ADD PARTITION (PARTITION p6 VALUES LESS THAN (2020));
```

### HASH 和 KEY 分区的 Management

`HASH` 分区和 `KEY` 分区的添加和删除类似。

不能以 `RANGE` 或 `LIST` 分区的方式删除由 `HASH` 或 `KEY` 分区的 `table` 的分区。但是，您可以使用 `ALTER TABLE ... COALESCE PARTITION` 语句合并 `HASH` 或 `KEY` 分区。

假设您有一个包含有关 `Client` 端数据的 `table` ，该 `table` 分为十二个分区。 `clients table` 的定义如下所示：

```
CREATE TABLE clients (
    id INT,
    fname VARCHAR(30),
    lname VARCHAR(30),
    signed DATE
)
PARTITION BY HASH( MONTH(signed) )
PARTITIONS 12;
```

**将分区的数量从十二个减少到八个：**
```
ALTER TABLE clients COALESCE PARTITION 4;
```

`COALESCE` 在按 `HASH`，`KEY` ，`LINEAR HASH` 或 `LINEAR KEY` 分区的 `table` 同样适用。

> 注意：`COALESCE PARTITION` 后面的数字是要合并为其余部分的分区的数量，换句话说，这是要从 `table` 中删除的分区的数量，所以不能大于分区总数。

### 用 table 交换分区和子分区

可以使用 `ALTER TABLE pt EXCHANGE PARTITION p WITH TABLE nt` 让 `table` 交换 `table` 分区或子分区。

#### 用未分区 table 交换分区

假设已使用以下 `SQL` 语句创建并填充了分区 `table` `e`：
```
CREATE TABLE e (
    id INT NOT NULL,
    fname VARCHAR(30),
    lname VARCHAR(30)
)
    PARTITION BY RANGE (id) (
        PARTITION p0 VALUES LESS THAN (50),
        PARTITION p1 VALUES LESS THAN (100),
        PARTITION p2 VALUES LESS THAN (150),
        PARTITION p3 VALUES LESS THAN (MAXVALUE)
);

INSERT INTO e VALUES
    (1669, "Jim", "Smith"),
    (337, "Mary", "Jones"),
    (16, "Frank", "White"),
    (2005, "Linda", "Black");
```

创建名为 `e2` 的 `e` 的非分区副本：
```
mysql> CREATE TABLE e2 LIKE e;
Query OK, 0 rows affected (1.34 sec)

mysql> ALTER TABLE e2 REMOVE PARTITIONING;
Query OK, 0 rows affected (0.90 sec)
Records: 0  Duplicates: 0  Warnings: 0
```

将 `tablee` 中的分区 `p0` 与 `tablee2` 交换：
```
mysql> ALTER TABLE e EXCHANGE PARTITION p0 WITH TABLE e2;
Query OK, 0 rows affected (0.28 sec)
```

与分区交换的 `table` 不一定必须为空，插入数据再交换：
```
mysql> INSERT INTO e VALUES (41, "Michael", "Green");
Query OK, 1 row affected (0.05 sec)

mysql> ALTER TABLE e EXCHANGE PARTITION p0 WITH TABLE e2;
Query OK, 0 rows affected (0.28 sec)
```

#### Nonmatching Rows

在发出 `ALTER TABLE ...` 交换分区语句之前在未分区 `table` 中找到的任何行都必须满足将它们存储在目标分区中的条件；否则，该语句将失败。

未分区前， `e2` 可以存着任何数据，交互分区后， `51` 不在分区范围内，所以失败。

```
mysql> INSERT INTO e2 VALUES (51, "Ellen", "McDonald");
Query OK, 1 row affected (0.08 sec)

mysql> ALTER TABLE e EXCHANGE PARTITION p0 WITH TABLE e2;
ERROR 1707 (HY000): Found row that does not match the partition
```

#### 交换分区而不按行验证

为了避免在将分区与具有很多行的 `table` 交换分区时花费大量时间进行验证，可以通过将 `WITHOUT VALIDATION` 附加到 `ALTER TABLE ...` 交换分区语句来跳过逐行验证步骤。

#### 用未分区 table 交换子分区

您还可以使用 `ALTER TABLE ...` 交换分区语句将子分区 `table` 的子分区与未分区 `table` 交换。

创建一个 `tablees` ，该 `table` 由 `RANGE` 分区，并由 `KEY` 子分区，像处理 `tablee` 一样填充该 `table` ，然后创建该 `table` 的空的，未分区的副本 `es2` ，如下所示：
```
mysql> CREATE TABLE es (
    ->     id INT NOT NULL,
    ->     fname VARCHAR(30),
    ->     lname VARCHAR(30)
    -> )
    ->     PARTITION BY RANGE (id)
    ->     SUBPARTITION BY KEY (lname)
    ->     SUBPARTITIONS 2 (
    ->         PARTITION p0 VALUES LESS THAN (50),
    ->         PARTITION p1 VALUES LESS THAN (100),
    ->         PARTITION p2 VALUES LESS THAN (150),
    ->         PARTITION p3 VALUES LESS THAN (MAXVALUE)
    ->     );
Query OK, 0 rows affected (2.76 sec)

mysql> INSERT INTO es VALUES
    ->     (1669, "Jim", "Smith"),
    ->     (337, "Mary", "Jones"),
    ->     (16, "Frank", "White"),
    ->     (2005, "Linda", "Black");
Query OK, 4 rows affected (0.04 sec)
Records: 4  Duplicates: 0  Warnings: 0

mysql> CREATE TABLE es2 LIKE es;
Query OK, 0 rows affected (1.27 sec)

mysql> ALTER TABLE es2 REMOVE PARTITIONING;
Query OK, 0 rows affected (0.70 sec)
Records: 0  Duplicates: 0  Warnings: 0
```

尽管在创建 `tablees` 时我们没有明确命名任何子分区，但是当从该 `table` 中进行选择时，我们可以通过从 `INFORMATION_SCHEMA` 中包含 `PARTITIONS table` 的 `SUBPARTITION_NAME` 来获得这些子分区的生成名称，如下所示：
```
mysql> SELECT PARTITION_NAME, SUBPARTITION_NAME, TABLE_ROWS
    ->     FROM INFORMATION_SCHEMA.PARTITIONS
    ->     WHERE TABLE_NAME = 'es';
+----------------+-------------------+------------+
| PARTITION_NAME | SUBPARTITION_NAME | TABLE_ROWS |
+----------------+-------------------+------------+
| p0             | p0sp0             |          1 |
| p0             | p0sp1             |          0 |
| p1             | p1sp0             |          0 |
| p1             | p1sp1             |          0 |
| p2             | p2sp0             |          0 |
| p2             | p2sp1             |          0 |
| p3             | p3sp0             |          3 |
| p3             | p3sp1             |          0 |
+----------------+-------------------+------------+
8 rows in set (0.00 sec)
```

以下 `ALTER TABLE` 语句将子分区 `p3sp0 tablees` 与未分区 `tablee s2` 交换：
```
mysql> ALTER TABLE es EXCHANGE PARTITION p3sp0 WITH TABLE es2;
Query OK, 0 rows affected (0.29 sec)
```

### 分区维护

可以使用语句 `CHECK TABLE` ， `OPTIMIZE TABLE` ， `ANALYZE TABLE` 和 `REPAIR TABLE` 来完成分区 `table` 的 `table` 维护。

**重建分区**

这与删除存储在分区中的所有记录，然后重新插入它们的效果相同，可用于整理分区碎片。
```
ALTER TABLE t1 REBUILD PARTITION p0, p1;
```

**优化分区**

如果您从分区中删除了大量行，或者对具有可变长度行(即具有 `VARCHAR` ， `BLOB` 或 `TEXT` 列) 的分区 `table` 进行了许多更改，可以使用 `ALTER table...` 优化分区回收任何未使用的空间并对分区数据文件进行碎片整理。
```
ALTER TABLE t1 OPTIMIZE PARTITION p0, p1;
```

在给定分区上使用 `OPTIMIZE PARTITION` 等效于在该分区上运行 `CHECK PARTITION` ， `ANALYZE PARTITION` 和 `REPAIR PARTITION` 。

**分析分区**

读取并存储分区的密钥分布。
```
ALTER TABLE t1 ANALYZE PARTITION p3;
```

**修复分区**

修复损坏的分区。
```
ALTER TABLE t1 REPAIR PARTITION p0,p1;
```

**检查分区**

可以像对未分区 `table` 使用 `CHECK TABLE` 一样检查分区中的错误。
```
ALTER TABLE trb3 CHECK PARTITION p1;
```

### 获取有关分区的信息

- 使用显示创建 `table` 语句查看在创建分区 `table` 中使用的分区子句。

    ```
    SHOW CREATE TABLE tbl_name
    ```

- 使用显示 `table` 格状态语句确定 `table` 是否已分区。

    ```
    SHOW TABLE STATUS
        [{FROM | IN} db_name]
        [LIKE 'pattern' | WHERE expr]
    ```

- 查询 `INFORMATION_SCHEMA.PARTITIONS table`。

    ```
    SELECT * FROM INFORMATION_SCHEMA.PARTITIONS
       WHERE TABLE_NAME='tp' AND TABLE_SCHEMA='test';
    ```

- 使用语句 `EXPLAIN SELECT` 查看给定的 `SELECT` 使用了哪些分区。

    ```
    EXPLAIN SELECT * FROM table WHERE type=1;
    ```