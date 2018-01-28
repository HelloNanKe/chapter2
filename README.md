chapter2:

数据库及表
```sql
CREATE TABLE customer
(
  id        INT AUTO_INCREMENT
    PRIMARY KEY,
  name      VARCHAR(255) NOT NULL
  COMMENT '客户名称',
  contact   VARCHAR(255) NOT NULL
  COMMENT '联系人',
  telephone VARCHAR(255) NULL
  COMMENT '电话号码',
  email     VARCHAR(255) NULL
  COMMENT '邮箱',
  remark    TEXT         NULL
  COMMENT '备注'
);

```