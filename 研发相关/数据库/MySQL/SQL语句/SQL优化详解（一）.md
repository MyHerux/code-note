# MySql 优化详解（一）EXPLAIN分析

## 慢 SQL 的定义

> 慢 `sql` 在广义上指执行速度很慢的 `sql` 语句，具体指查询时间大于指定慢查询时间的查询

- 查看设置的慢查询时间

    > show variables like 'long_query_time';  

- 修改慢查询的时间

    > set global long_query_time=1;  

## 查找慢查询

- 将慢查询记录到日志中

    - 查看慢查询记录日志是否开启

        > show variables like 'slow%';  

    - 开启慢查询记录

        > set global slow_query_log=ON;  

    - 日志文件地址

        ![mark](http://of0qa2hzs.bkt.clouddn.com/blog/171208/id5bAKjdC8.png?imageslim)

- 使用 `Druid` 的 `sql` 分析

    - 配置 `Druid` 连接池

    - 访问 `/druid/index.html` 地址

        ![mark](http://of0qa2hzs.bkt.clouddn.com/blog/171208/mj4HEfG382.png?imageslim)

## EXPLAIN 命令

> `MySQL` 的 `EXPLAIN` 命令用于 `SQL` 语句的查询执行计划 `(QEP)`。这条命令的输出结果能够让我们了解 `MySQL` 优化器是如何执行 `SQL` 语句的。这条命令并没有提供任何调整建议，但它能够提供重要的信息帮助你做出调优决策。

- 语法

    > `MySQL` 的 `EXPLAIN` 语法可以运行在 `SELECT` 语句或者特定表上（5.6之后允许 `EXPLAIN` 非 `SELECT` 查询）。

    ```
    UPDATE city SET name='chengdu' WHERE id=1
    ```

    ==>

    ```
    SELECT name FROM city WHERE id=1
    ```

- 命令详解

    ![mark](http://of0qa2hzs.bkt.clouddn.com/blog/171208/ckC8cD14c1.png?imageslim)

    QEP中的列表参数：
    - `id`

        > 查询语句的标识符，如果有零时表则为NULL。

    - `select_type`

        > 查询类型。包含的类型有：
        - `SIMPLE`：简单的查询（没有使用 `UNION` 或子查询）
        - `PRIMARY`：查询中最外层的 `SELECT`（如两表做 `UNION` 或者存在子查询的外层的表操作为 `PRIMARY` ，内层的操作为 `UNION` ）
        - `UNION`：`UNION` 操作中，查询中处于内层的 `SELECT`（内层的 `SELECT` 语句与外层的 `SELECT` 语句没有依赖关系）
        - `DEPENDENT UNION`：`UNION` 操作中，查询中处于内层的 `SELECT`（内层的 `SELECT` 语句与外层的 `SELECT` 语句有依赖关系）
        - `UNION RESULT`：`UNION` 操作的结果，`id` 值通常为 `NULL`
        - `SUBQUERY`：子查询中首个 `SELECT`（如果有多个子查询存在）
        - `DEPENDENT SUBQUERY`：子查询中首个 `SELECT` ，但依赖于外层的表（如果有多个子查询存在）
        - `DERIVED`：被驱动的 `SELECT` 子查询（子查询位于 `FROM` 子句）
        - `UNCACHEABLE SUBQUERY`：对于外层的主表，子查询不可被物化，每次都需要计算（耗时操作）
        - `UNCACHEABLE UNION`：`UNION` 操作中，内层的不可被物化的子查询（类似于 `UNCACHEABLE SUBQUERY`）

    - `table`

        > 输出行所用到的表的名称。也可能是以下形式：
        - `<unionM,N>` : 该行是具有 `M` 和 `N` 的 `id` 值的行的联合行
        - `<derivedN>` : 该行引用 `id` 值为 `N` 的行的派生表结果
    - `partitions`
        
        > 记录将与查询匹配的分区,这一列只有在 `EXPLAIN PARTITIONS` 语法中才会出现

    - `type`

        > 代表QEP 中指定的表使用的连接方式。下面是最常用的几种连接方式：
        - `const`: MySql对查询的部分进行优化并将其转换为一个常亮时， `system` 这是 `const`的特例，当表只有一个 `row` 时会出现
            ```sql
            SELECT * FROM tbl_name WHERE primary_key=1;

            SELECT * FROM tbl_name
            WHERE primary_key_part1=1 AND primary_key_part2=2;
            ```
        - `eq_ref`: 索引查找，MySql知道最多返回一条符合条件的记录。`eq_ref`可用于使用 `=运算符` 进行比较的索引列.
            ```sql
            SELECT * FROM ref_table,other_table
            WHERE ref_table.key_column=other_table.column;

            SELECT * FROM ref_table,other_table
            WHERE ref_table.key_column_part1=other_table.column
            AND ref_table.key_column_part2=1;
            ```
        - `ref`：索引查找，返回所有匹配某个单个值的行,`ref`可以用于使用`=` 或`<=>运算符`进行比较的索引列
            ```sql
            SELECT * FROM ref_table WHERE key_column=expr;

            SELECT * FROM ref_table,other_table
            WHERE ref_table.key_column=other_table.column;

            SELECT * FROM ref_table,other_table
            WHERE ref_table.key_column_part1=other_table.column
            AND ref_table.key_column_part2=1;
            ```
        - `range`：有限制的索引扫描,`range` 可以用于使用任何 `=，<>，>，> =，<，<=，IS NULL，<=>，BETWEEN或IN（）运算符` 将键列与常量进行比较
            ```sql
            SELECT * FROM tbl_name
            WHERE key_column = 10;

            SELECT * FROM tbl_name
            WHERE key_column BETWEEN 10 and 20;

            SELECT * FROM tbl_name
            WHERE key_column IN (10,20,30);

            SELECT * FROM tbl_name
            WHERE key_part1 = 10 AND key_part2 IN (10,20,30);
            ```
        - `ALL`：这个值表示需要一次全表扫描，其他类型的值还有 `fulltext 、ref_or_null 、index_merge 、unique_subquery、index_subquery 以及index`。

        - `index`：和全表扫描一样，只是MySql扫描表时按索引次序进行而不是行。优点是避免了排序；缺点是要承担索引次序读取整个表的开销。
    - `possible_keys`

        > 可以从中选择查找表中的行的索引，如果这个列是 `NULL`，那么没有相关的索引

    - `key`

        > `MySQL` 实际决定使用的关键字（索引）。如果 `MySQL` 决定使用其中一个 `possible_keys` 索引来查找行，则该索引被列为关键值。如果所有的 `possible_keys` 都不合适，也可能选取其他的 `key` 

    - `key_len`

        > `MySQL` 实际使用的关键字（索引）长度

    - `ref`

        >  `ref` 列可以被用来标识那些用来进行索引比较的列或者常量。

    - `rows`

        > `rows` 表示 `MySQL` 认为它必须检查以执行查询的行数。

    - `filtered` (这一列只有在EXPLAINED EXTENDED 语法中才会出现)

        > `filtered` 表示将由表条件过滤的表行的估计百分比。

    - `Extra` 

        > 有关MySQL如何解析查询的其他信息

        - Using where 

            表示 `Mysql` 将在 `storage engine` 检索行后再进行过滤。
        - Using index 

            仅使用索引树中的信息从表中检索列信息，而不需要进行附加搜索来读取实际行(使用二级覆盖索引即可获取数据)。 当查询仅使用作为单个索引的一部分的列时，可以使用此策略。 
            ![mark](http://of0qa2hzs.bkt.clouddn.com/blog/171214/2152f4f269.png?imageslim)
        - Using index condition

            `Using index condition` 会先条件过滤索引，过滤完索引后找到所有符合索引条件的数据行，随后用 `WHERE` 子句中的其他条件去过滤这些数据行。
            ![mark](http://of0qa2hzs.bkt.clouddn.com/blog/171214/98K35C50f7.png?imageslim)
            因为 `MySQL` 的架构原因，分成了 `server层` 和 `引擎层` ，才有所谓的 `“下推（push down）”` 的说法。所以ICP其实就是实现了 `index filter` 技术，将原来的在 `server层` 进行的table filter中可以进行index filter的部分，在引擎层面使用index filter进行处理，不再需要回表进行table filter
            ![mark](http://of0qa2hzs.bkt.clouddn.com/blog/171214/6gEjE00a3h.png?imageslim)

        - Using filesort 

            当 `Query` 中包含 `ORDER BY` 操作，而且无法利用索引完成排序操作的时候，`MySQL Query Optimizer` 不得不选择相应的排序算法来实现。数据较少时从内存排序，否则从磁盘排序。

        - Using temporary 

            要解决查询，`MySQL` 需要创建一个临时表来保存结果。 如果查询包含不同列的 `GROUP BY` 和 `ORDER BY` 子句，则通常会发生这种情况。

        - Using join buffer (Block Nested Loop), Using join buffer (Batched Key Access)

            `Block Nested-Loop Join`算法：将外层循环的行/结果集存入 `join buffer` , 内层循环的每一行与整个 `buffer` 中的记录做比较，从而减少内层循环的次数。
            `Batched Key Access`算法：对于多表 `join` 语句，当 `MySQL` 使用索引访问第二个 `join` 表的时候，使用一个 `join buffer` 来收集第一个操作对象生成的相关列值。BKA构建好 `key` 后，批量传给引擎层做索引查找。

## 使用 EXPLAIN 命令进行分析

#### 查询子句

- `where` 条件查询

    - 比较运算符

        - 主键判等，`const` 等级
        ![mark](http://of0qa2hzs.bkt.clouddn.com/blog/171213/gIad1CG4Kd.png?imageslim)
        - 主键比较，`range` 等级       
        ![mark](http://of0qa2hzs.bkt.clouddn.com/blog/171213/605Lb3Cmg4.png?imageslim)
        - 索引查询，`ref` 等级
        ![mark](http://of0qa2hzs.bkt.clouddn.com/blog/171213/69Hi90JA5d.png?imageslim)
        - `IN` 查询，会全表扫描
        ![mark](http://of0qa2hzs.bkt.clouddn.com/blog/171213/eIDCIacF38.png?imageslim)
    - 逻辑运算符

        - `and ( && )` 逻辑与
        ![mark](http://of0qa2hzs.bkt.clouddn.com/blog/171213/HblaLBcGca.png?imageslim)

        - `not ( ! )` 逻辑非
        ![mark](http://of0qa2hzs.bkt.clouddn.com/blog/171213/g5856eJ4h3.png?imageslim)

        - `or ( || )` 逻辑或，会全表扫描
        ![mark](http://of0qa2hzs.bkt.clouddn.com/blog/171213/J88LKhk40J.png?imageslim)
    - 模糊查询

        - `like` 右模糊会全表扫描，左模糊无法使用索引
        ![mark](http://of0qa2hzs.bkt.clouddn.com/blog/171213/9jGk4CA77D.png?imageslim)
        ![mark](http://of0qa2hzs.bkt.clouddn.com/blog/171213/CbLLgeFe26.png?imageslim)
- `group by` 分组

    ![mark](http://of0qa2hzs.bkt.clouddn.com/blog/171213/JdJJ55a1kK.png?imageslim)

- `having` 筛选

- `order by` 排序

    - 主键排序，主键自带排序

        ![mark](http://of0qa2hzs.bkt.clouddn.com/blog/171214/kJcfhDcB1G.png?imageslim)

    - 索引排序，非主键的排序需要：`Using filesort`，`phone` 索引无法完成排序操作

        ![mark](http://of0qa2hzs.bkt.clouddn.com/blog/171214/g0EDBma2li.png?imageslim)

    - 一般排序，非主键的排序需要：`Using filesort`

        ![mark](http://of0qa2hzs.bkt.clouddn.com/blog/171214/KcF4mk2EGa.png?imageslim)

- `limit` 条数限制

#### 子查询

- `where` 型子查询（把内层查询结果当作外层查询的比较条件）

    ![mark](http://of0qa2hzs.bkt.clouddn.com/blog/171214/7mBd7KEJJl.png?imageslim)

- `from` 型子查询（把内层的查询结果供外层再次查询)

    ![mark](http://of0qa2hzs.bkt.clouddn.com/blog/171215/Ikk6DaG6c0.png?imageslim)

- `exists`型子查询（把外层查询结果拿到内层，看内层的查询是否成立）

    ![mark](http://of0qa2hzs.bkt.clouddn.com/blog/171215/AeJ02mD9JE.png?imageslim)

#### UNION
- `UNION` 查询

    ![mark](http://of0qa2hzs.bkt.clouddn.com/blog/171213/FKGEmDlaBh.png?imageslim)

    - 因为需要移除相同数据，所以需要额外的零时表（`Using temporary`）

- `UNION ALL` 查询

    ![mark](http://of0qa2hzs.bkt.clouddn.com/blog/171213/Ib2beHgILh.png?imageslim)

#### 左连接，右连接，内连接

- `LEFT JOIN`

    ![mark](http://of0qa2hzs.bkt.clouddn.com/blog/171218/KhhiaJD02A.png?imageslim)

- `RIGHT JOIN`

    a left join b 等价于 b right join a
    推荐使用左连接代替右连接

- `INNER JOIN`

    ![mark](http://of0qa2hzs.bkt.clouddn.com/blog/171218/LiIJ7FEAJF.png?imageslim)