## 分区类型

- RANGE 分区

  根据给定范围内的列值将行分配给分区。

- LIST 分区

  与 `RANGE` 分区类似，区别在于分区是根据与一组离散值之一匹配的列选择的。

- HASH 分区

  使用这种类型的分区时，将根据用户定义的 `table` 达式返回的值来选择一个分区，该 `table` 达式对要插入 `table` 中的行中的列值进行运算。该函数可以包含在 `MySQL` 中有效的任何产生非负整数值的 `table` 达式。

- KEY 分区

  这种分区类型类似于 HASH 进行分区，不同之处在于仅提供了一个或多个要评估的列，并且 `MySQL` 服务器提供了自己的哈希函数。这些列可以包含非整数值，因为 `MySQL` 提供的哈希函数可以保证整数结果，而与列数据类型无关。

### RANGE 分区

基于属于一个给定连续区间的列值，把多行分配给分区。范围应该是连续的，但不能重叠，并使用 `VALUES LESS THAN` 运算符定义。最常见的是基于时间字段. 基于分区的列最好是整型，如果日期型的可以使用函数转换为整型。

Example:

```
CREATE TABLE employees (
    id INT NOT NULL,
    fname VARCHAR(30),
    lname VARCHAR(30),
    hiredate DATETIME NOT NULL DEFAULT '1970-01-01 00:00:00',
    separated DATE NOT NULL DEFAULT '9999-12-31',
    job_code INT NOT NULL,
    store_id INT NOT NULL,
    updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
)
```

可以根据需要以多种方式对该 `table` 进行分区，使用 `store_id` 列进行分区：

```
CREATE TABLE employees (
    id INT NOT NULL,
    fname VARCHAR(30),
    lname VARCHAR(30),
    hiredate DATETIME NOT NULL DEFAULT '1970-01-01 00:00:00',
    separated DATE NOT NULL DEFAULT '9999-12-31',
    job_code INT NOT NULL,
    store_id INT NOT NULL,
    updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
)
PARTITION BY RANGE (store_id) (
    PARTITION p0 VALUES LESS THAN (6),
    PARTITION p1 VALUES LESS THAN (11),
    PARTITION p2 VALUES LESS THAN (16),
    PARTITION p3 VALUES LESS THAN (21)
);
```

> 注意：每个分区的定义 Sequences 是从最低到最高。这是 PARTITION BY RANGE 语法的要求。

对于上述的分区方式，很明显，新数据：`(72, 'Mitchell', 'Wilson', '1998-06-25', NULL, 13)` 会被插入到 `p2` 中。但是如果插入 `store_id>=21` 的数据则会报错，因为服务器不知道将其放置在何处。所以，新的建表语句如下：

```
CREATE TABLE employees (
    id INT NOT NULL,
    fname VARCHAR(30),
    lname VARCHAR(30),
    hiredate DATETIME NOT NULL DEFAULT '1970-01-01 00:00:00',
    separated DATE NOT NULL DEFAULT '9999-12-31',
    job_code INT NOT NULL,
    store_id INT NOT NULL,
    updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
)
PARTITION BY RANGE (store_id) (
    PARTITION p0 VALUES LESS THAN (6),
    PARTITION p1 VALUES LESS THAN (11),
    PARTITION p2 VALUES LESS THAN (16),
    PARTITION p3 VALUES LESS THAN MAXVALUE
);
```

> 注意：对于找不到分区的数据插入，可以使用`INSERT IGNORE`。使用 `INSERT IGNORE` 时，对于包含不匹配值的行，插入操作将以静默方式失败，匹配的行将被插入。

**手动操作分区**

- 新增分区

    随着业务的进行，可能当前分区不满足需求，可以手动添加新的分区：

    ```
    ALTER TABLE employees ADD PARTITION (PARTITION p4 VALUES LESS THAN (32));
    ```

    支持分区 `table` 的重命名。您可以使用 `ALTER TABLE ... REORGANIZE PARTITION` 间接重命名各个分区；但是，此操作将复制分区的数据。

- 清空分区

    同时，分区的好处之一是可以方便的删除数据，比如，要删除分区 `p0` 中的所有数据

    ```
    ALTER TABLE employees TRUNCATE PARTITION p0;
    ```

    > 注意：`TRUNCATE` 只会清空数据，不会删除分区，如果要删除分区使用 `DROP`

    该语句的等效 `DELETE` 语句为：

    ```
    DELETE FROM employees WHERE store_id < 6;
    ```

    删除多个不连续分区比使用 `DELETE` 语句更简洁，而且效率更高，比如：

    ```
    ALTER TABLE employees TRUNCATE PARTITION p0,p2;
    ```

    该语句的等效 `DELETE` 语句为：

    ```
    DELETE FROM employees WHERE (store_id>=0 AND store_id < 6) or (store_id>=11 AND store_id<16);
    ```

**基于时间间隔的分区方案**

- 用 `RANGE` 对 `table` 进行分区，对于分区 `table` 达式，采用对 `DATE` ， `TIME` 或 `DATETIME` 列进行操作并返回整数值的函数（必须返回整数），如下所示：

  ```
  CREATE TABLE employees (
      id INT NOT NULL,
      fname VARCHAR(30),
      lname VARCHAR(30),
      hiredate DATETIME NOT NULL DEFAULT '1970-01-01 00:00:00',
      separated DATE NOT NULL DEFAULT '9999-12-31',
      job_code INT NOT NULL,
      store_id INT NOT NULL,
      updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
  )
  PARTITION BY RANGE( YEAR(separated) ) (
      PARTITION p0 VALUES LESS THAN (1960),
      PARTITION p1 VALUES LESS THAN (1970),
      PARTITION p2 VALUES LESS THAN (1980),
      PARTITION p3 VALUES LESS THAN (1990),
      PARTITION p4 VALUES LESS THAN MAXVALUE
  );
  ```

  ```
  CREATE TABLE employees (
      id INT NOT NULL,
      fname VARCHAR(30),
      lname VARCHAR(30),
      hiredate DATETIME NOT NULL DEFAULT '1970-01-01 00:00:00',
      separated DATE NOT NULL DEFAULT '9999-12-31',
      job_code INT NOT NULL,
      store_id INT NOT NULL,
      updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
  )
  PARTITION BY RANGE( TO_DAYS(hiredate) ) (
      PARTITION p0 VALUES LESS THAN (1960),
      PARTITION p1 VALUES LESS THAN (1970),
      PARTITION p2 VALUES LESS THAN (1980),
      PARTITION p3 VALUES LESS THAN (1990),
      PARTITION p4 VALUES LESS THAN MAXVALUE
  );
  ```

  ```
  CREATE TABLE employees (
      id INT NOT NULL,
      fname VARCHAR(30),
      lname VARCHAR(30),
      hiredate DATETIME NOT NULL DEFAULT '1970-01-01 00:00:00',
      separated DATE NOT NULL DEFAULT '9999-12-31',
      job_code INT NOT NULL,
      store_id INT NOT NULL,
      updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
  )
  PARTITION BY RANGE( UNIX_TIMESTAMP(updated_time) ) (
      PARTITION p0 VALUES LESS THAN ( UNIX_TIMESTAMP('2008-01-01 00:00:00') ),
      PARTITION p1 VALUES LESS THAN ( UNIX_TIMESTAMP('2008-04-01 00:00:00') ),
      PARTITION p2 VALUES LESS THAN ( UNIX_TIMESTAMP('2008-07-01 00:00:00') ),
      PARTITION p3 VALUES LESS THAN ( UNIX_TIMESTAMP('2008-10-01 00:00:00') ),
      PARTITION p4 VALUES LESS THAN MAXVALUE
  );
  ```

- 使用 `DATE` 或 `DATETIME` 列作为分区列，通过 `RANGE COLUMNS` 对 `table` 进行分区。

  ```
  CREATE TABLE employees (
      id INT NOT NULL,
      fname VARCHAR(30),
      lname VARCHAR(30),
      hiredate DATETIME NOT NULL DEFAULT '1970-01-01 00:00:00',
      separated DATE NOT NULL DEFAULT '9999-12-31',
      job_code INT NOT NULL,
      store_id INT NOT NULL,
      updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
  )
  PARTITION BY RANGE COLUMNS(separated) (
      PARTITION p0 VALUES LESS THAN ('1960-01-01'),
      PARTITION p1 VALUES LESS THAN ('1970-01-01'),
      PARTITION p2 VALUES LESS THAN ('1980-01-01'),
      PARTITION p3 VALUES LESS THAN ('1990-01-01'),
      PARTITION p4 VALUES LESS THAN MAXVALUE
  );
  ```

  > 注意：`RANGE COLUMNS` 不支持使用除 `DATE` 或 `DATETIME` 以外的日期或时间类型的分区列。

### LIST 分区

`LIST` 分区和 `RANGE` 分区类似，区别在于 `LIST` 是枚举值列表的集合， `RANGE` 是连续的区间值的集合。二者在语法方面非常的相似。

Example:

```
CREATE TABLE employees (
    id INT NOT NULL,
    fname VARCHAR(30),
    lname VARCHAR(30),
    hiredate DATETIME NOT NULL DEFAULT '1970-01-01 00:00:00',
    separated DATE NOT NULL DEFAULT '9999-12-31',
    job_code INT NOT NULL,
    store_id INT NOT NULL,
    updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
)
```

假设不同的 `employees` 的 `store_id` 分布在不同的区域。

| Region  | Store Id             |
| ------- | -------------------- |
| North   | 3, 5, 6, 9, 17       |
| East    | 1, 2, 10, 11, 19, 20 |
| West    | 4, 12, 13, 14, 18    |
| Central | 7, 8, 15, 16         |

要将 `table` 分区，以使属于同一区域的 `Store` 的行存储在同一分区：

```
CREATE TABLE employees (
    id INT NOT NULL,
    fname VARCHAR(30),
    lname VARCHAR(30),
    hiredate DATETIME NOT NULL DEFAULT '1970-01-01 00:00:00',
    separated DATE NOT NULL DEFAULT '9999-12-31',
    job_code INT NOT NULL,
    store_id INT NOT NULL,
    updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
)
PARTITION BY LIST(store_id) (
    PARTITION pNorth VALUES IN (3,5,6,9,17),
    PARTITION pEast VALUES IN (1,2,10,11,19,20),
    PARTITION pWest VALUES IN (4,12,13,14,18),
    PARTITION pCentral VALUES IN (7,8,15,16)
);
```

**手动操作分区**

`LIST`分区在 `table` 中添加或删除分区的方式与 `RANGE` 分区类似。

`ALTER TABLE employees TRUNCATE PARTITION pWest` 删除与在该区域的 `Store` 工作的员工有关的所有行，该查询比等效的 `DELETE` 语句 `DELETE FROM employees WHERE store_id IN (4,12,13,14,18)`;

> 注意：与 `RANGE` 分区的情况不同，`LIST`分区不存在 `MAXVALUE` 之类的“包罗万象”；分区 `table` 达式的所有期望值都应包含在 `PARTITION ... VALUES IN (...)`子句中

### HASH 分区

`HASH` 分区主要用于确保在 `sched` 数量的分区之间均匀分布数据。对于 `RANGE` 或 `LIST` 分区，必须明确指定将给定列值或一组列值存储在哪个分区中。而没有明显可以分区的特征字段，可以使用散列分区。基于给定的分区个数，将数据分配到不同的分区， `HASH` 分区只能针对整数进行 `HASH` ，对于非整形的字段只能通过表达式将其转换成整数。表达式可以是 `mysql` 中任意有效的函数或者表达式，对于非整形的 `HASH` 往表插入数据的过程中会多一步表达式的计算操作，所以不建议使用复杂的表达式这样会影响性能。

Example：

```
CREATE TABLE employees (
    id INT NOT NULL,
    fname VARCHAR(30),
    lname VARCHAR(30),
    hired DATE NOT NULL DEFAULT '1970-01-01',
    separated DATE NOT NULL DEFAULT '9999-12-31',
    job_code INT,
    store_id INT
)
PARTITION BY HASH(store_id)
PARTITIONS 4;
```

如果不包括 `PARTITIONS` 子句，则分区数默认为 `1`（使用 `PARTITIONS` 关键字后不带数字会导致语法错误）。

使用表达式返回整数：

```
CREATE TABLE employees (
    id INT NOT NULL,
    fname VARCHAR(30),
    lname VARCHAR(30),
    hired DATE NOT NULL DEFAULT '1970-01-01',
    separated DATE NOT NULL DEFAULT '9999-12-31',
    job_code INT,
    store_id INT
)
PARTITION BY HASH( YEAR(hired) )
PARTITIONS 4;
```

当使用 `PARTITION BY HASH` 时，`MySQL` 根据 `table` 达式结果的 `Mod` 值来确定要使用哪个分区。假设 tablet1的定义如下，因此它具有 4 个分区：

```
CREATE TABLE t1 (col1 INT, col2 CHAR(5), col3 DATE)
    PARTITION BY HASH( YEAR(col3) )
    PARTITIONS 4;
```

如果将记录插入到 `col3` 值为 `'2005-09-15'` 的 `t1` 中，则存储记录的分区将确定如下：

```
MOD(YEAR('2005-09-01'),4)
=  MOD(2005,4)
=  1
```

### LINEAR HASH 分区

`LINEAR HASH`分区是 `HASH` 分区的一种特殊类型，与 `HASH` 分区是基于 `MOD` 函数不同的是，它基于的是另外一种算法：`linear powers-of-two algorithm`

Example：

```
CREATE TABLE employees (
    id INT NOT NULL,
    fname VARCHAR(30),
    lname VARCHAR(30),
    hired DATE NOT NULL DEFAULT '1970-01-01',
    separated DATE NOT NULL DEFAULT '9999-12-31',
    job_code INT,
    store_id INT
)
PARTITION BY LINEAR HASH( YEAR(hired) )
PARTITIONS 4;
```

算法过程（本质是用 `&` 运算替代 `Mod` 运算）：

- 找到与分区数 `num` 相关的数字 `V`

    ```
    V = POWER(2, CEILING(LOG(2, num)))
    ```
    > 假定 `num` 为13， LOG(2,13) = 3.7004397181411。 CEILING(3.7004397181411) = 4， V = POWER(2,4) = 16

- Set N = F ( column_list ) & ( V - 1).

- While N >= num :

- Set V = V / 2

- Set N = N & ( V - 1)

Example：

```
CREATE TABLE t1 (col1 INT, col2 CHAR(5), col3 DATE)
    PARTITION BY LINEAR HASH( YEAR(col3) )
    PARTITIONS 6;
```

现在，假设您要向 `t1` 插入两个记录，这些记录的 `col3` 列值为`'2003-04-14'` 和 `'1998-10-19'`。其中第一个的分区号确定如下：

```
V = POWER(2, CEILING( LOG(2,6) )) = 8
N = YEAR('2003-04-14') & (8 - 1)
   = 2003 & 7
   = 3

(3 >= 6 is FALSE: record stored in partition #3)
```

计算第二条记录所在的分区号，如下所示：

```
V = 8
N = YEAR('1998-10-19') & (8 - 1)
  = 1998 & 7
  = 6

(6 >= 6 is TRUE: additional step required)

N = 6 & ((8 / 2) - 1)
  = 6 & 3
  = 2

(2 >= 6 is FALSE: record stored in partition #2)
```

通过线性哈希进行分区的优势是在数据量大的场景（`&` 操作消耗远小于 `Mod` 函数），譬如TB级，增加、删除、合并和拆分分区会更快，缺点是，相对于 `HASH` 分区，它数据分布不均匀的概率更大。最好选择数量为2的幂的分区（2、4、8、16等），否则中间分区的大小往往是外部分区的两倍

### KEY 分区

类似于按 `HASH` 分区，区别在于 `KEY` 分区支持计算一列或多列，且MySQL服务器提供其自身的哈希函数。

主要区别：

- `KEY` 分区允许多列，而 `HASH` 分区只允许一列。

- 如果在有主键或者唯一键的情况下， `key` 中分区列可不指定，默认为主键或者唯一键，如果没有，则必须显性指定列。

- `KEY` 分区对象必须为列，而不能是基于列的表达式。

- `KEY` 分区和 `HASH` 分区的算法不一样，`PARTITION BY HASH (expr)`，`MOD` 取值的对象是 `expr` 返回的值，而 `PARTITION BY KEY (column_list)`，基于的是列的MD5值。

Example:

```
CREATE TABLE k1 (
    id INT NOT NULL PRIMARY KEY,
    name VARCHAR(20)
)
PARTITION BY KEY()
PARTITIONS 2;
```

也可以通过线性键对 table 进行分区。

```
CREATE TABLE tk (
    col1 INT NOT NULL,
    col2 CHAR(5),
    col3 DATE
)
PARTITION BY LINEAR KEY (col1)
PARTITIONS 3;
```

使用 `LINEAR` 对 `KEY` 分区具有与 `HASH` 分区相同的效果，分区算法相同，参考 [LINEAR-HASH-分区](#linear-hash-分区 "LINEAR-HASH-分区")

### Subpartitioning

子分区(也称为复合分区)是分区 `table` 中每个分区的进一步划分。

Example：

```
CREATE TABLE ts (id INT, purchased DATE)
    PARTITION BY RANGE( YEAR(purchased) )
    SUBPARTITION BY HASH( TO_DAYS(purchased) )
    SUBPARTITIONS 2 (
        PARTITION p0 VALUES LESS THAN (1990),
        PARTITION p1 VALUES LESS THAN (2000),
        PARTITION p2 VALUES LESS THAN MAXVALUE
    );
```

`tablets` 具有 `3` 个 `RANGE` 分区。每个分区 `p0` ， `p1` 和 `p2` 进一步分为 `2` 个子分区。实际上，整个 `table` 分为 `3 * 2 = 6` 个分区。前 `2` 个子分区仅在 `purchased` 列中存储值小于 `1990` 的那些记录。

在 `MySQL 5.7` 中，可以对被 `RANGE` 或 `LIST` 分区的 `table` 进行子分区。子分区可以使用 `HASH` 或 `KEY` 分区。这也称为复合分区。

> 注意：`SUBPARTITION BY HASH` 和 `SUBPARTITION BY KEY` 通常分别遵循与 `PARTITION BY HASH` 和 `PARTITION BY KEY` 。特殊的是 `SUBPARTITION BY KEY` (与 `PARTITION BY KEY` 不同)当前不支持默认列，因此即使 `table` 具有显式主键，也必须指定用于此目的的列。

也可以使用SUBPARTITION子句显式定义子分区，以指定各个子分区的选项。

Example：

```
CREATE TABLE ts (id INT, purchased DATE)
    PARTITION BY RANGE( YEAR(purchased) )
    SUBPARTITION BY HASH( TO_DAYS(purchased) ) (
        PARTITION p0 VALUES LESS THAN (1990) (
            SUBPARTITION s0,
            SUBPARTITION s1
        ),
        PARTITION p1 VALUES LESS THAN (2000) (
            SUBPARTITION s2,
            SUBPARTITION s3
        ),
        PARTITION p2 VALUES LESS THAN MAXVALUE (
            SUBPARTITION s4,
            SUBPARTITION s5
        )
    );
```

注意事项：

- 每个分区必须具有相同数量的子分区。

- 如果在分区 `table` 的任何分区上使用 `SUBPARTITION` 显式定义任何子分区，则必须全部定义它们。

- 每个 `SUBPARTITION` 子句必须(至少)包括该子分区的名称。否则，您可以为子分区设置任何所需的选项，或允许其采用该选项的默认设置。

- 子分区名称在整个 `table` 中必须唯一

子分区可以与特别大的 `MyISAM table` 一起使用，以在许多磁盘上分配数据和索引。假设您安装了 `6` 个磁盘，分别为 `/disk0` ，`/disk1` ，/disk2` 等。

Example:

```
CREATE TABLE ts (id INT, purchased DATE)
    ENGINE = MYISAM
    PARTITION BY RANGE( YEAR(purchased) )
    SUBPARTITION BY HASH( TO_DAYS(purchased) ) (
        PARTITION p0 VALUES LESS THAN (1990) (
            SUBPARTITION s0
                DATA DIRECTORY = '/disk0/data'
                INDEX DIRECTORY = '/disk0/idx',
            SUBPARTITION s1
                DATA DIRECTORY = '/disk1/data'
                INDEX DIRECTORY = '/disk1/idx'
        ),
        PARTITION p1 VALUES LESS THAN (2000) (
            SUBPARTITION s2
                DATA DIRECTORY = '/disk2/data'
                INDEX DIRECTORY = '/disk2/idx',
            SUBPARTITION s3
                DATA DIRECTORY = '/disk3/data'
                INDEX DIRECTORY = '/disk3/idx'
        ),
        PARTITION p2 VALUES LESS THAN MAXVALUE (
            SUBPARTITION s4
                DATA DIRECTORY = '/disk4/data'
                INDEX DIRECTORY = '/disk4/idx',
            SUBPARTITION s5
                DATA DIRECTORY = '/disk5/data'
                INDEX DIRECTORY = '/disk5/idx'
        )
    );
```

> 注意：如果 `1990` 之前的数据过大，你可以分配更多的磁盘给该分区。

### MySQL 分区如何处理 NULL

在 `MySQL` 中进行分区并不能禁止 `NULL` 作为分区 `table` 表达式的值，无论它是列值还是用户提供的 `table` 表达式的值。即使允许使用 `NULL` 作为必须以其他方式产生整数的 `table` 表达式的值，也要记住 `NULL` 不是数字。 `MySQL` 的分区实现将 `NULL` 视为小于任何非 `NULL` 值，就像 `ORDERBY` 一样。

**使用 `RANGE` 分区处理 `NULL`**

如果将行插入到由 `RANGE` 分区的 `table` 中，使得用于确定分区的列值为 `NULL` ，则该行将插入最低的分区。

Example：

```
CREATE TABLE t1 (
         c1 INT,
         c2 VARCHAR(20)
     )
PARTITION BY RANGE(c1) (
    PARTITION p0 VALUES LESS THAN (0),
    PARTITION p1 VALUES LESS THAN (10),
    PARTITION p2 VALUES LESS THAN MAXVALUE
);
```

> 注意：`NULL` 将会被插入到最低的分区，即 `p0` 分区。

Example：

```
CREATE TABLE t2 (
         c1 INT,
         c2 VARCHAR(20)
     )
PARTITION BY RANGE(c1) (
    PARTITION p0 VALUES LESS THAN (-5),
    PARTITION p1 VALUES LESS THAN (0),
    PARTITION p2 VALUES LESS THAN (10),
    PARTITION p3 VALUES LESS THAN MAXVALUE
);
```

> 注意：`NULL` 不是数字，所以并不是插入到 `p1` 分区，而是被插入到最低的分区，即 `p0` 分区。

**使用 `LIST` 分区处理 `NULL`**

当且仅当使用包含 `NULL` 的值列 `table` 定义了分区之一时，被 `LIST` 分区的 `table` 才允许 `NULL` 值。相反的是，用 `LIST` 分区的 `table` 未在值列 `table` 中显式使用 `NULL` ，该 `table` 将拒绝为分区 `table` 达式生成 `NULL` 值的行。

Example：

```
mysql> CREATE TABLE ts1 (
    ->     c1 INT,
    ->     c2 VARCHAR(20)
    -> )
    -> PARTITION BY LIST(c1) (
    ->     PARTITION p0 VALUES IN (0, 3, 6),
    ->     PARTITION p1 VALUES IN (1, 4, 7),
    ->     PARTITION p2 VALUES IN (2, 5, 8)
    -> );
Query OK, 0 rows affected (0.01 sec)

mysql> INSERT INTO ts1 VALUES (9, 'mothra');
ERROR 1504 (HY000): Table has no partition for value 9

mysql> INSERT INTO ts1 VALUES (NULL, 'mothra');
ERROR 1504 (HY000): Table has no partition for value NULL
```

只能将 `c1` 值介于 `0` 和 `8` 之间的行插入 `ts1` 。 `NULL` 和 `9` 落在该范围之外，所以不能插入。我们可以创建 `tablets2` 和 `ts3` ，其值列 `table` 包含 `NULL` ，如下所示：

```
mysql> CREATE TABLE ts2 (
    ->     c1 INT,
    ->     c2 VARCHAR(20)
    -> )
    -> PARTITION BY LIST(c1) (
    ->     PARTITION p0 VALUES IN (0, 3, 6),
    ->     PARTITION p1 VALUES IN (1, 4, 7),
    ->     PARTITION p2 VALUES IN (2, 5, 8),
    ->     PARTITION p3 VALUES IN (NULL)
    -> );
Query OK, 0 rows affected (0.01 sec)

mysql> INSERT INTO ts2 VALUES (NULL, 'mothra');
Query OK, 1 row affected (0.00 sec)
```

`NULL` 被插入到 `p3` 分区。

**使用 `HASH` 和 `KEY` 分区处理 `NULL`**

对于 `HASH` 或 `KEY` 分区的 `table` ， `NULL` 的处理方式有所不同。在这些情况下，任何产生 `NULL` 值的分区 `table` 达式都将被视为其返回值为零。

>`HASH` 和 `KEY` 的函数对于 `NULL` 都是返回 `0`