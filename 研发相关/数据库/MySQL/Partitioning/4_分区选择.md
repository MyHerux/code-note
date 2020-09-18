## 分区选择

`MySQL 5.7` 支持显式选择分区和子分区，当执行一条语句时，应检查该分区和子分区中是否存在与给定 `WHERE` 条件匹配的行。分区选择类似于分区修剪，因为只检查特定的分区是否匹配，但是在两个关键方面有所不同：

- 与要自动执行的分区修剪不同，要检查的分区由语句的发布者指定。

- 分区修剪仅适用于查询，而查询和许多 `DML` 语句均支持显式选择分区。

支持显式分区选择的 SQL 语句：

- SELECT

    ```
    SELECT * FROM employees PARTITION (p1);
    ```

- DELETE

    ```
    DELETE FROM employees PARTITION (p0, p1)
             WHERE fname LIKE 'j%';
    ```

- INSERT

- REPLACE

    对于插入行的语句，其行为不同之处在于未能找到合适的分区会导致语句失败。对于 `INSERT` 和 `REPLACE` 语句都是如此，如下所示：
    ```
    mysql> INSERT INTO employees PARTITION (p2) VALUES (20, 'Jan', 'Jones', 1, 3);
    ERROR 1729 (HY000): Found a row not matching the given partition set
    mysql> INSERT INTO employees PARTITION (p3) VALUES (20, 'Jan', 'Jones', 1, 3);
    Query OK, 1 row affected (0.07 sec)

    mysql> REPLACE INTO employees PARTITION (p0) VALUES (20, 'Jan', 'Jones', 3, 2);
    ERROR 1729 (HY000): Found a row not matching the given partition set

    mysql> REPLACE INTO employees PARTITION (p3) VALUES (20, 'Jan', 'Jones', 3, 2);
    Query OK, 2 rows affected (0.09 sec)
    ```

- UPDATE

- LOAD DATA.

- LOAD XML.